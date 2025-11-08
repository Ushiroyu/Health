package com.community.health.auth.entity;

import com.community.health.common.security.Role;
import jakarta.persistence.*;
import lombok.*;
import com.community.health.common.security.AesGcmStringConverter;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;
  @Column(nullable=false, unique=true, length=50)
  private String username;
  @Column(nullable=false, length=100)
  private String password;
  @Enumerated(EnumType.STRING)
  @Column(nullable=false, length=20)
  private Role role;
  private String name;
  private String gender;
  private Integer age;
  private String phone;
  @Convert(converter = AesGcmStringConverter.class)
  private String email;
  @Column(length = 20)
  private String status; // ENABLED/DISABLED/LOCKED
}
