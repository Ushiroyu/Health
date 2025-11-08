package com.community.health.appointment.notify;

import com.community.health.appointment.entity.NotifyInbox;
import com.community.health.appointment.repo.NotifyInboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InboxSender implements NotificationSender {
  private static final Logger log = LoggerFactory.getLogger(InboxSender.class);
  private final NotifyInboxRepository repo;
  public InboxSender(NotifyInboxRepository r){ this.repo = r; }
  @Override
  public void send(Long userId, String title, String content) {
    repo.save(NotifyInbox.builder().userId(userId).title(title).content(content).createdAt(LocalDateTime.now()).readFlag(false).build());
    log.info("inbox notify user={} title={}", userId, title);
  }
}

