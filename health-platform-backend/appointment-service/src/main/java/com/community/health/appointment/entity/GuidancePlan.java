package com.community.health.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "guidance_plan")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuidancePlan {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long planId;
  private Long userId;
  private Long doctorId;
  private String type; // BP/BG/WEIGHT/OTHER
  @Lob
  private String rules; // JSON: {"target":"120/80","note":"低盐饮食"}
  private String frequency; // DAILY/WEEKLY
  private String dailyTime; // HH:mm
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private String status; // ACTIVE/PAUSED/ENDED
}

