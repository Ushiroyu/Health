package com.community.health.auth.service;

import com.community.health.auth.entity.User;
import com.community.health.auth.repo.UserRepository;
import com.community.health.common.api.ApiResponse;
import com.community.health.common.exception.ErrorCodes;
import com.community.health.common.security.JwtUtil;
import com.community.health.common.security.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class AuthService {
  private final UserRepository repo;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private final JwtUtil jwtUtil;

  public AuthService(UserRepository repo,
                     @Value("${security.jwt.secret}") String secret,
                     @Value("${security.jwt.ttl}") long ttl) {
    this.repo = repo;
    this.jwtUtil = new JwtUtil(secret, ttl);
  }

  @PostConstruct
  public void initAdmin() {
    repo.findByUsername("admin").orElseGet(() ->
      repo.save(User.builder()
        .username("admin")
        .password(encoder.encode("admin123"))
        .role(Role.ADMIN)
        .name("系统管理员")
        .status("ENABLED")
        .build())
    );
  }

  public ApiResponse<Map<String, Object>> register(String username, String password, Role role) {
    String normalizedUsername = username == null ? null : username.trim();
    if (!StringUtils.hasText(normalizedUsername) || !StringUtils.hasText(password)) {
      return new ApiResponse<>(ErrorCodes.UNPROCESSABLE, "用户名或密码不能为空", null);
    }
    if (password.length() < 6) {
      return new ApiResponse<>(ErrorCodes.UNPROCESSABLE, "密码长度需不少于 6 位", null);
    }
    Role finalRole = role == null ? Role.RESIDENT : role;
    if (finalRole != Role.RESIDENT) {
      return new ApiResponse<>(ErrorCodes.FORBIDDEN, "仅管理员可创建该类型账号", null);
    }
    if (repo.findByUsername(normalizedUsername).isPresent()) {
      return new ApiResponse<>(ErrorCodes.USERNAME_EXISTS, "用户名已存在", null);
    }
    User u = repo.save(User.builder()
      .username(normalizedUsername)
      .password(encoder.encode(password))
      .role(finalRole)
      .status("ENABLED")
      .build());
    return ApiResponse.ok(Map.of("userId", u.getUserId()));
  }

  public ApiResponse<Map<String, Object>> login(String username, String password) {
    User u = repo.findByUsername(username).orElse(null);
    if (u == null || !encoder.matches(password, u.getPassword())) {
      return ApiResponse.error("用户名或密码错误");
    }
    String token = jwtUtil.createToken(u.getUserId(), u.getUsername(), u.getRole());
    return ApiResponse.ok(Map.of("token", token, "role", u.getRole(), "userId", u.getUserId()));
  }
}
