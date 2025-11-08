package com.community.health.appointment.mq;

import com.community.health.appointment.repo.UserAccountRepository;
import com.community.health.appointment.notify.NotificationSender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
@RocketMQMessageListener(topic = "activity-events", consumerGroup = "health-activity")
public class ActivityEventConsumer implements RocketMQListener<String> {
  private static final Logger log = LoggerFactory.getLogger(ActivityEventConsumer.class);
  private final ObjectMapper mapper = new ObjectMapper();
  private final List<NotificationSender> senders;
  private final UserAccountRepository userRepo;

  public ActivityEventConsumer(List<NotificationSender> senders,
                               UserAccountRepository userRepo) {
    this.senders = senders;
    this.userRepo = userRepo;
  }

  @Override
  public void onMessage(String message) {
    log.info("[activity-events] {}", message);
    try {
      JsonNode node = mapper.readTree(message);
      String status = node.path("status").asText("");
      if (status != null && !status.equalsIgnoreCase("PUBLISHED")) {
        return;
      }
      String title = node.path("title").asText("社区活动");
      String eventDate = node.path("eventDate").asText(null);
      String location = node.path("location").asText(null);
      String content = buildContent(title, eventDate, location);

      Set<Long> receivers = new HashSet<>();
      var activeUsers = userRepo.findByRoleIgnoreCaseAndStatusNot("RESIDENT", "DISABLED");
      if (activeUsers.isEmpty()) {
        userRepo.findByRoleIgnoreCase("RESIDENT").forEach(u -> receivers.add(u.getUserId()));
      } else {
        activeUsers.forEach(u -> receivers.add(u.getUserId()));
      }
      if (receivers.isEmpty()) return;

      for (Long uid : receivers) {
        for (NotificationSender sender : senders) {
          sender.send(uid, "社区活动通知", content);
        }
      }
    } catch (Exception e) {
      log.warn("Failed to consume activity-event {}", message, e);
    }
  }

  private String buildContent(String title, String eventDate, String location) {
    StringBuilder sb = new StringBuilder("即将举行的社区活动：《").append(title).append("》");
    if (eventDate != null && !eventDate.isBlank()) {
      sb.append("，时间：").append(eventDate);
    }
    if (location != null && !location.isBlank()) {
      sb.append("，地点：").append(location);
    }
    sb.append("。欢迎报名参加！");
    return sb.toString();
  }
}
