package com.community.health.appointment.mq;

import com.community.health.appointment.repo.HealthProfileViewRepository;
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
@RocketMQMessageListener(topic = "content-events", consumerGroup = "health-content")
public class ContentEventConsumer implements RocketMQListener<String> {
  private static final Logger log = LoggerFactory.getLogger(ContentEventConsumer.class);
  private final ObjectMapper mapper = new ObjectMapper();
  private final List<NotificationSender> senders;
  private final HealthProfileViewRepository profileRepo;
  private final UserAccountRepository userRepo;

  public ContentEventConsumer(List<NotificationSender> senders,
                              HealthProfileViewRepository profileRepo,
                              UserAccountRepository userRepo) {
    this.senders = senders;
    this.profileRepo = profileRepo;
    this.userRepo = userRepo;
  }

  @Override
  public void onMessage(String message) {
    log.info("[content-events] {}", message);
    try {
      JsonNode node = mapper.readTree(message);
      String status = node.path("status").asText("");
      if (status != null && status.equalsIgnoreCase("DRAFT")) {
        return; // 不推送草稿
      }
      String title = node.path("title").asText("健康资讯更新");
      String category = node.path("category").asText(null);
      Set<Long> receivers = new HashSet<>();
      if (category != null && !category.isBlank()) {
        profileRepo.findByChronicConditionsContainingIgnoreCase(category).forEach(p -> {
          if (p.getUserId() != null) {
            receivers.add(p.getUserId());
          }
        });
      }
      if (receivers.isEmpty()) {
        var activeUsers = userRepo.findByRoleIgnoreCaseAndStatusNot("RESIDENT", "DISABLED");
        if (activeUsers.isEmpty()) {
          userRepo.findByRoleIgnoreCase("RESIDENT").forEach(u -> receivers.add(u.getUserId()));
        } else {
          activeUsers.forEach(u -> receivers.add(u.getUserId()));
        }
      }
      if (receivers.isEmpty()) {
        return;
      }
      String content = buildContent(title, category);
      for (Long uid : receivers) {
        for (NotificationSender sender : senders) {
          sender.send(uid, "健康资讯推荐", content);
        }
      }
    } catch (Exception e) {
      log.warn("Failed to consume content-event {}", message, e);
    }
  }

  private String buildContent(String title, String category) {
    if (category != null && !category.isBlank()) {
      return "为您推送与[" + category + "]相关的健康资讯：《" + title + "》";
    }
    return "为您推荐新的健康资讯：《" + title + "》";
  }
}
