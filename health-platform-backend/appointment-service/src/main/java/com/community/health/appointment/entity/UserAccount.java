package com.community.health.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserAccount {
  @Id
  @Column(name = "user_id")
  private Long userId;

  private String username;
  private String role;
  private String status;
}

