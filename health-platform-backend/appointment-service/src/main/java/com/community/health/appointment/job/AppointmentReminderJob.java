package com.community.health.appointment.job;

import com.community.health.appointment.entity.Appointment;
import com.community.health.appointment.repo.AppointmentRepository;
import com.community.health.appointment.notify.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class AppointmentReminderJob {
  private static final Logger log = LoggerFactory.getLogger(AppointmentReminderJob.class);
  private final AppointmentRepository apptRepo;
  private final NotificationSender sender;
  private final StringRedisTemplate redis;

  public AppointmentReminderJob(AppointmentRepository a, NotificationSender s, StringRedisTemplate r){ this.apptRepo = a; this.sender = s; this.redis = r; }

  @Scheduled(cron = "0 */5 * * * *")
  public void remindUpcoming(){
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = today.plusDays(1);
    handleDay(today);
    handleDay(tomorrow);
  }

  private void handleDay(LocalDate day){
    List<Appointment> list = apptRepo.findAll((root, q, cb) -> cb.and(
      cb.equal(root.get("apptDate"), day),
      cb.equal(root.get("status"), "booked")
    ));
    LocalDateTime now = LocalDateTime.now();
    for (Appointment a : list){
      try {
        String start = a.getApptTime();
        if (start == null || !start.contains("-")) continue;
        String startStr = start.split("-")[0];
        LocalTime t = LocalTime.parse(startStr);
        LocalDateTime apptStart = LocalDateTime.of(a.getApptDate(), t);
        long minutes = java.time.Duration.between(now, apptStart).toMinutes();
        if (minutes <= 60 && minutes >= 0) {
          String key = "notify:appt:" + a.getApptId();
          Boolean ok = redis.opsForValue().setIfAbsent(key, "1", 2, TimeUnit.DAYS);
          if (ok != null && ok) {
            String title = "就诊提醒";
            String content = "您预约的就诊将于 " + a.getApptDate() + " " + startStr + " 开始，请准时到达。";
            sender.send(a.getUserId(), title, content);
            log.info("sent reminder apptId={} userId={}", a.getApptId(), a.getUserId());
          }
        }
      } catch (Exception ignore) {}
    }
  }
}
