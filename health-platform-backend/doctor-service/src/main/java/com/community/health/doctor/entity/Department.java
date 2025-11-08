package com.community.health.doctor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "department")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer deptId;
  @Column(nullable=false, length=100)
  private String deptName;
  @Lob
  private String description;
}
