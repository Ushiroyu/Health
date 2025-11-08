package com.community.health.doctor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "doctor")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Doctor {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long doctorId;
  private Long userId;
  private Integer deptId;
  private String title;
  private String specialty;
  @Lob
  private String scheduleInfo;
  @Column(length = 20)
  private String status;
}
