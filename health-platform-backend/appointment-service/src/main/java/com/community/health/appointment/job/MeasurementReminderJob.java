package com.community.health.appointment.job;

import com.community.health.appointment.repo.MeasurementReminderRepository;
import com.community.health.appointment.notify.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MeasurementReminderJob {
  private static final Logger log = LoggerFactory.getLogger(MeasurementReminderJob.class);
  private final MeasurementReminderRepository repo;
  private final NotificationSender sender;
  private final StringRedisTemplate redis;
  public MeasurementReminderJob(MeasurementReminderRepository r, NotificationSender s, StringRedisTemplate t){ this.repo = r; this.sender = s; this.redis = t; }

  @Scheduled(cron = "0 * * * * *") // 每分钟执行
  public void tick(){
    String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    List<com.community.health.appointment.entity.MeasurementReminder> list = repo.findByEnabledTrueAndDailyTime(now);
    for (var r : list){
      String key = "remind:measure:" + r.getReminderId() + ":" + LocalDate.now();
      Boolean ok = redis.opsForValue().setIfAbsent(key, "1", 25, TimeUnit.HOURS);
      if (ok != null && ok) {
        String title = "测量提醒";
        String content = "请在"+ now +" 进行" + r.getType() + "测量并记录";
        sender.send(r.getUserId(), title, content);
        log.info("measure reminder user={} type={} time={}", r.getUserId(), r.getType(), now);
      }
    }
  }
}

