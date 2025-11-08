package com.community.health.appointment.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "notifications.email", name = "enabled", havingValue = "true")
public class EmailSender implements NotificationSender {
  private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
  @Override
  public void send(Long userId, String title, String content) {
    // TODO integrate with real email gateway; here we just log
    log.info("[EMAIL] user={} {} - {}", userId, title, content);
  }
}

