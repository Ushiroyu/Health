package com.community.health.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import com.community.health.common.security.AesGcmStringConverter;
import java.time.LocalDateTime;

@Entity @Table(name = "prescription")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prescription {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long prescId;
  private Long appointmentId;
  private Long doctorId;
  private Long userId;
  private LocalDateTime prescDate;
  @Lob
  private String medicines; // JSON array string
  @Lob
  @Convert(converter = AesGcmStringConverter.class)
  private String advice;
}
