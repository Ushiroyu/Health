package com.community.health.activity.mq;

import com.community.health.activity.entity.CommunityEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
public class ActivityEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(ActivityEventPublisher.class);
  private final RocketMQTemplate template;
  private final ObjectMapper mapper = new ObjectMapper();
  private final String topic;

  public ActivityEventPublisher(RocketMQTemplate template,
                                @Value("${health.topics.activity:activity-events}") String topic) {
    this.template = template;
    this.topic = topic;
  }

  public void publishEvent(CommunityEvent event) {
    if (event == null) {
      return;
    }
    Map<String, Object> payload = new HashMap<>();
    payload.put("eventId", event.getEventId());
    payload.put("title", event.getTitle());
    payload.put("status", event.getStatus());
    if (event.getEventDate() != null) {
      payload.put("eventDate", event.getEventDate().format(DateTimeFormatter.ISO_DATE_TIME));
    }
    payload.put("location", event.getLocation());
    payload.put("capacity", event.getCapacity());
    try {
      template.convertAndSend(topic, mapper.writeValueAsString(payload));
      log.info("Published activity-event {}", payload);
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize activity event for event {}", event.getEventId(), e);
    }
  }
}
