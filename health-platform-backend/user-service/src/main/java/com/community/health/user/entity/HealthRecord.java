package com.community.health.user.entity;

import jakarta.persistence.*;
import lombok.*;
import com.community.health.common.security.AesGcmStringConverter;
import java.time.LocalDate;

@Entity @Table(name = "health_record")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HealthRecord {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long recordId;
  private Long userId;
  private LocalDate recordDate;
  private String type;
  private String value;
  @Convert(converter = AesGcmStringConverter.class)
  private String note;
}
