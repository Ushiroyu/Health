package com.community.health.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notify_inbox")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotifyInbox {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notifyId;
  private Long userId;
  private String title;
  @Lob
  private String content;
  private LocalDateTime createdAt;
  private Boolean readFlag;
}

