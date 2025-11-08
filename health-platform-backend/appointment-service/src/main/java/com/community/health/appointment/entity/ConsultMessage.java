package com.community.health.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "consult_message")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsultMessage {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long msgId;
  private Long sessionId;
  private String senderType; // DOCTOR/USER
  private String contentType; // text/image/file
  @Lob
  private String content; // text content or URL
  private LocalDateTime createdAt;
  private Boolean read;
}

