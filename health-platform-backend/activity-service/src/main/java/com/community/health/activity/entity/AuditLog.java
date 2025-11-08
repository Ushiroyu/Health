package com.community.health.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private LocalDateTime ts;
  private Long userId;
  private String action;
  private String targetType;
  private String targetId;
  @Lob private String details;
  private String ip;
}

