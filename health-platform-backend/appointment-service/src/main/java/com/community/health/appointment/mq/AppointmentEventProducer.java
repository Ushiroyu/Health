package com.community.health.appointment.mq;

import com.community.health.appointment.event.AppointmentEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
public class AppointmentEventProducer {
  private static final Logger log = LoggerFactory.getLogger(AppointmentEventProducer.class);
  private final RocketMQTemplate rocketMQTemplate;
  private final ObjectMapper mapper = new ObjectMapper();
  public AppointmentEventProducer(RocketMQTemplate t){ this.rocketMQTemplate = t; }

  public void send(String topic, AppointmentEvent event){
    try {
      String payload = mapper.writeValueAsString(event);
      rocketMQTemplate.convertAndSend(topic, payload);
      log.info("Sent appointment-event to topic={} payload={}", topic, payload);
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize appointment event {}", event, e);
    } catch (Exception ex) {
      log.error("Failed to publish appointment event {}", event, ex);
    }
  }
}
