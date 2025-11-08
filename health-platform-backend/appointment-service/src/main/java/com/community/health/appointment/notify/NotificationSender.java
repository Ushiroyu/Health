package com.community.health.appointment.notify;

public interface NotificationSender {
  void send(Long userId, String title, String content);
}

