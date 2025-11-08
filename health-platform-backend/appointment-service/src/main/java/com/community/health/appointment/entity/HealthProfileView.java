package com.community.health.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "health_profile")
@Getter
@Setter
public class HealthProfileView {
  @Id
  @Column(name = "profile_id")
  private Long profileId;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "chronic_conditions")
  private String chronicConditions;
}

