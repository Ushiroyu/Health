package com.community.health.appointment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "measurement_reminder")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MeasurementReminder {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long reminderId;
  private Long userId;
  private String type; // BP/BG/WEIGHT/HR/TEMP
  private String dailyTime; // HH:mm
  private Boolean enabled;
}

