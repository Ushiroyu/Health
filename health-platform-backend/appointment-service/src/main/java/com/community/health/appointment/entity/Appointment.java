package com.community.health.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import com.community.health.common.security.AesGcmStringConverter;
import java.time.LocalDate;

@Entity @Table(name = "appointment", uniqueConstraints = @UniqueConstraint(name="uniq_doctor_time", columnNames={"doctorId","apptDate","apptTime"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long apptId;
  private Long userId;
  private Long doctorId;
  private LocalDate apptDate;
  private String apptTime; // e.g. 10:00-10:30
  private String status;   // booked/canceled/completed
  @Convert(converter = AesGcmStringConverter.class)
  private String symptom;
}
