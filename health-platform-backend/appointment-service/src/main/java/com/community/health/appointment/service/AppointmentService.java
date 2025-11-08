package com.community.health.appointment.service;

import com.community.health.appointment.entity.Appointment;
import com.community.health.appointment.repo.AppointmentRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class AppointmentService {
  private final AppointmentRepository repo;
  private final com.community.health.appointment.repo.ScheduleRepository scheduleRepo;
  private final StringRedisTemplate redis;
  @org.springframework.beans.factory.annotation.Autowired(required = false)
  private com.community.health.appointment.mq.AppointmentEventProducer producer;
  @org.springframework.beans.factory.annotation.Autowired(required = false)
  private com.community.health.appointment.repo.AuditLogRepository auditRepo;
  public AppointmentService(AppointmentRepository r, com.community.health.appointment.repo.ScheduleRepository s, StringRedisTemplate t){ this.repo = r; this.scheduleRepo = s; this.redis = t; }

  private String slotKey(Long doctorId, LocalDate date, String time){ return "slot:"+doctorId+":"+date+":"+time; }
  private String idemKey(Long userId, Long doctorId, LocalDate date, String time){ return "idem:"+userId+":"+doctorId+":"+date+":"+time; }

  private static final String LUA_RESERVE =
      "local slot = KEYS[1]\n" +
      "local idem = KEYS[2]\n" +
      "local idemTtl = ARGV[1]\n" +
      "if redis.call('EXISTS', idem) == 1 then return -2 end\n" +
      "if redis.call('EXISTS', slot) == 0 then return -3 end\n" +
      "local left = tonumber(redis.call('GET', slot))\n" +
      "if not left or left <= 0 then return -1 end\n" +
      "left = redis.call('DECR', slot)\n" +
      "if left < 0 then redis.call('INCR', slot); return -1 end\n" +
      "redis.call('SET', idem, '1', 'PX', idemTtl)\n" +
      "return left\n";

  @Transactional
  public ApiResponse<Map<String,Object>> book(Long userId, Long doctorId, LocalDate date, String time, String symptom){
    String key = slotKey(doctorId, date, time);
    String idem = idemKey(userId, doctorId, date, time);
    // 必须有排班
    var scheduleOpt = scheduleRepo.findByDoctorIdAndDateAndTimeSlot(doctorId, date, time);
    if (scheduleOpt.isEmpty() || (scheduleOpt.get().getStatus()!=null && !scheduleOpt.get().getStatus().equalsIgnoreCase("OPEN"))) {
      return ApiResponse.error("号源未开放");
    }
    if (redis.opsForValue().get(key) == null) {
      int capacity = Math.max(0, scheduleOpt.get().getCapacity()==null?0:scheduleOpt.get().getCapacity());
      long booked = repo.countByDoctorIdAndApptDateAndApptTimeAndStatus(doctorId, date, time, "booked");
      int left = Math.max(0, capacity - (int)booked);
      redis.opsForValue().set(key, String.valueOf(left));
    }

    Long res = redis.execute((connection) -> (Long) connection.scriptingCommands().eval(
        LUA_RESERVE.getBytes(),
        ReturnType.INTEGER,
        2,
        key.getBytes(), idem.getBytes(), String.valueOf(TimeUnit.MINUTES.toMillis(2)).getBytes()
    ), true);

    if (res == null) throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.APPT_TEMP_UNAVAILABLE, "预约暂不可用");
    if (res == -2) throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.APPT_DUPLICATE, "请勿重复提交");
    if (res == -1) throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.APPT_NO_SLOT, "号源不足");
    if (res == -3) throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.APPT_NOT_OPEN, "号源未开放");

    try {
      Appointment a = repo.save(Appointment.builder()
        .userId(userId).doctorId(doctorId).apptDate(date).apptTime(time)
        .status("booked").symptom(symptom).build());
      Map<String,Object> data = new HashMap<>();
      data.put("apptId", a.getApptId());
      data.put("left", res);
      if (producer != null) {
        var ev = new com.community.health.appointment.event.AppointmentEvent();
        ev.setType("BOOKED"); ev.setApptId(a.getApptId()); ev.setUserId(userId); ev.setDoctorId(doctorId); ev.setDate(date); ev.setTime(time); ev.setTs(System.currentTimeMillis());
        producer.send("appointment-events", ev);
      }
      if (auditRepo != null) {
        auditRepo.save(com.community.health.appointment.entity.AuditLog.builder().ts(java.time.LocalDateTime.now()).userId(userId).action("BOOK_APPOINTMENT").targetType("APPOINTMENT").targetId(String.valueOf(a.getApptId())).details("doctor="+doctorId+",date="+date+",time="+time).build());
      }
      return ApiResponse.ok(data);
    } catch (DataIntegrityViolationException e){
      redis.opsForValue().increment(key);
      redis.delete(idem);
      throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.APPT_DUPLICATE, "该时段已被预约");
    } catch (Exception e){
      redis.opsForValue().increment(key);
      redis.delete(idem);
      throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.APPT_TEMP_UNAVAILABLE, "预约失败，请稍后重试");
    }
  }

  public ApiResponse<?> cancel(Long userId, Long apptId){
    return repo.findById(apptId).map(a -> {
      if (!a.getUserId().equals(userId)) throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.APPT_FORBIDDEN_OPERATION, "无权取消");
      a.setStatus("canceled");
      repo.save(a);
      redis.opsForValue().increment(slotKey(a.getDoctorId(), a.getApptDate(), a.getApptTime()));
      if (producer != null) {
        var ev = new com.community.health.appointment.event.AppointmentEvent();
        ev.setType("CANCELED"); ev.setApptId(a.getApptId()); ev.setUserId(userId); ev.setDoctorId(a.getDoctorId()); ev.setDate(a.getApptDate()); ev.setTime(a.getApptTime()); ev.setTs(System.currentTimeMillis());
        producer.send("appointment-events", ev);
      }
      if (auditRepo != null) {
        auditRepo.save(com.community.health.appointment.entity.AuditLog.builder().ts(java.time.LocalDateTime.now()).userId(userId).action("CANCEL_APPOINTMENT").targetType("APPOINTMENT").targetId(String.valueOf(a.getApptId())).build());
      }
      return ApiResponse.ok();
    }).orElseGet(() -> new ApiResponse<>(com.community.health.common.exception.ErrorCodes.APPT_NOT_FOUND, "预约不存在", null));
  }

  public Page<Appointment> my(Long userId, int page, int size, String status) {
    Pageable p = PageRequest.of(page, size);
    if (status == null || status.isBlank()) {
      return repo.findByUserIdOrderByApptDateDesc(userId, p);
    }
    return repo.findByUserIdAndStatusOrderByApptDateDesc(userId, status, p);
  }

  public Map<String, Integer> querySlots(Long doctorId, LocalDate date) {
    // 优先按排班返回所有时段，并确保Redis有值
    Map<String, Integer> result = new HashMap<>();
    var schedules = scheduleRepo.findByDoctorIdAndDateOrderByTimeSlotAsc(doctorId, date);
    for (var s : schedules) {
      String key = slotKey(doctorId, date, s.getTimeSlot());
      String v = redis.opsForValue().get(key);
      if (v == null) {
        int capacity = Math.max(0, s.getCapacity()==null?0:s.getCapacity());
        long booked = repo.countByDoctorIdAndApptDateAndApptTimeAndStatus(doctorId, date, s.getTimeSlot(), "booked");
        int left = Math.max(0, capacity - (int)booked);
        redis.opsForValue().set(key, String.valueOf(left));
        result.put(s.getTimeSlot(), left);
      } else {
        result.put(s.getTimeSlot(), Integer.parseInt(v));
      }
    }
    return result;
  }

  // for controller usage
  public void setSlot(Long doctorId, LocalDate date, String time, int left){
    redis.opsForValue().set(slotKey(doctorId, date, time), String.valueOf(left));
  }
  public int getSlot(Long doctorId, LocalDate date, String time, int defaultVal){
    String v = redis.opsForValue().get(slotKey(doctorId, date, time));
    if (v == null) return defaultVal;
    try { return Integer.parseInt(v);} catch (Exception e){ return defaultVal; }
  }

  public org.springframework.data.domain.Page<Appointment> search(Long doctorId, String dateFrom, String dateTo, String status, String symptom, int page, int size) {
    org.springframework.data.jpa.domain.Specification<Appointment> spec = (root, q, cb) -> {
      java.util.List<jakarta.persistence.criteria.Predicate> ps = new java.util.ArrayList<>();
      if (doctorId != null) ps.add(cb.equal(root.get("doctorId"), doctorId));
      if (status != null && !status.isBlank()) ps.add(cb.equal(root.get("status"), status));
      if (dateFrom != null && !dateFrom.isBlank()) ps.add(cb.greaterThanOrEqualTo(root.get("apptDate"), java.time.LocalDate.parse(dateFrom)));
      if (dateTo != null && !dateTo.isBlank()) ps.add(cb.lessThanOrEqualTo(root.get("apptDate"), java.time.LocalDate.parse(dateTo)));
      if (symptom != null && !symptom.isBlank()) ps.add(cb.like(root.get("symptom"), "%"+symptom+"%"));
      return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
    return repo.findAll(spec, org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "apptDate")));
  }
}
