package com.community.health.appointment.mq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
@RocketMQMessageListener(topic = "appointment-events", consumerGroup = "health-notify")
public class AppointmentEventConsumer implements RocketMQListener<String> {
  private static final Logger log = LoggerFactory.getLogger(AppointmentEventConsumer.class);
  private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
  private final java.util.List<com.community.health.appointment.notify.NotificationSender> senders;
  public AppointmentEventConsumer(java.util.List<com.community.health.appointment.notify.NotificationSender> s){ this.senders = s; }

  @Override
  public void onMessage(String message) {
    log.info("[appointment-events] {}", message);
    try {
      var node = mapper.readTree(message);
      Long userId = node.path("userId").isNumber() ? node.get("userId").asLong() : null;
      String type = node.path("type").asText();
      String date = node.path("date").asText();
      String time = node.path("time").asText();
      if (userId != null) {
        String title = type.equals("BOOKED")? "预约成功":"预约已取消";
        String content = type + " - 日期:" + date + " 时段:" + time;
        for (var sdr : senders) { sdr.send(userId, title, content); }
      }
    } catch (Exception ignore) {}
  }
}
