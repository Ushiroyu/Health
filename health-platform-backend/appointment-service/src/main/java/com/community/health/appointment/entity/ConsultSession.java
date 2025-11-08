package com.community.health.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "consult_session")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsultSession {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long sessionId;
  private Long userId;
  private Long doctorId;
  private String status; // OPEN/CLOSED
  private String chiefComplaint;
  private LocalDateTime createdAt;
  private LocalDateTime closedAt;
}

