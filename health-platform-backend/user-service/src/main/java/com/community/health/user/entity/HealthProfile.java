package com.community.health.user.entity;

import com.community.health.common.security.AesGcmStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人健康档案信息，属于 user-service 管理的居民域。
 */
@Entity
@Table(name = "health_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long profileId;

  @Column(nullable = false, unique = true)
  private Long userId;

  private String fullName;
  private String gender;
  private LocalDate birthDate;
  private String bloodType;

  @Convert(converter = AesGcmStringConverter.class)
  private String phone;

  @Convert(converter = AesGcmStringConverter.class)
  private String email;

  @Convert(converter = AesGcmStringConverter.class)
  private String address;

  @Convert(converter = AesGcmStringConverter.class)
  private String idNumber;

  @Column(length = 1024)
  private String chronicConditions;

  @Column(length = 1024)
  private String allergies;

  @Column(length = 1024)
  private String medicalHistory;

  @Column(length = 1024)
  private String medications;

  private String emergencyContact;

  @Convert(converter = AesGcmStringConverter.class)
  private String emergencyPhone;

  @Column(length = 1024)
  private String lifestyleNotes;

  private LocalDateTime lastUpdated;

  @PrePersist
  @PreUpdate
  public void touchLastUpdated() {
    this.lastUpdated = LocalDateTime.now();
  }
}

