package com.community.health.appointment.controller;

import com.community.health.appointment.entity.Schedule;
import com.community.health.appointment.repo.AppointmentRepository;
import com.community.health.appointment.repo.ScheduleRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/appointment/schedule")
public class ScheduleController {
  private final ScheduleRepository scheduleRepo;
  private final AppointmentRepository apptRepo;
  private final com.community.health.appointment.service.AppointmentService service;
  private final com.community.health.appointment.repo.AuditLogRepository auditRepo;

  public record BatchReq(Long doctorId, String date, List<String> timeSlots, Integer capacity){}

  @PostMapping("/batch")
  public ApiResponse<?> batch(@RequestHeader("X-User-Id") Long adminId, @RequestBody BatchReq req){
    LocalDate d = LocalDate.parse(req.date());
    if (req.timeSlots()==null || req.timeSlots().isEmpty())
      throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.UNPROCESSABLE, "时段不能为空");
    if (req.capacity()==null || req.capacity() <= 0)
      throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.UNPROCESSABLE, "容量需>0");
    for (String ts : req.timeSlots()){
      Schedule s = scheduleRepo.findByDoctorIdAndDateAndTimeSlot(req.doctorId(), d, ts).orElseGet(Schedule::new);
      s.setDoctorId(req.doctorId());
      s.setDate(d);
      s.setTimeSlot(ts);
      s.setCapacity(req.capacity());
      s.setStatus("OPEN");
      scheduleRepo.save(s);
      // 初始化Redis槽位：capacity - 已约
      long booked = apptRepo.countByDoctorIdAndApptDateAndApptTimeAndStatus(req.doctorId(), d, ts, "booked");
      int left = Math.max(0, req.capacity() - (int)booked);
      service.setSlot(req.doctorId(), d, ts, left);
    }
    auditRepo.save(com.community.health.appointment.entity.AuditLog.builder().ts(java.time.LocalDateTime.now()).userId(adminId).action("BATCH_SCHEDULE").targetType("DOCTOR").targetId(String.valueOf(req.doctorId())).details("date="+req.date()+",slots="+req.timeSlots()+",cap="+req.capacity()).build());
    return ApiResponse.ok();
  }

  @GetMapping
  public ApiResponse<?> list(@RequestParam("doctorId") Long doctorId,
                             @RequestParam("date") String date){
    LocalDate d = LocalDate.parse(date);
    List<Schedule> list = scheduleRepo.findByDoctorIdAndDateOrderByTimeSlotAsc(doctorId, d);
    // 返回附带剩余量
    List<Map<String,Object>> out = new ArrayList<>();
    for (Schedule s : list){
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("time", s.getTimeSlot());
      m.put("capacity", s.getCapacity());
      m.put("status", s.getStatus());
      int left = service.getSlot(doctorId, s.getDate(), s.getTimeSlot(), s.getCapacity());
      m.put("left", left);
      out.add(m);
    }
    return ApiResponse.ok(out);
  }

  public ScheduleController(ScheduleRepository s, AppointmentRepository a, com.community.health.appointment.service.AppointmentService svc, com.community.health.appointment.repo.AuditLogRepository ar){
    this.scheduleRepo = s; this.apptRepo = a; this.service = svc; this.auditRepo = ar; }
}
