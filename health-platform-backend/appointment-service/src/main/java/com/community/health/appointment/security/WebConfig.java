package com.community.health.appointment.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    var adminPaths = Set.of(
      "/appointment/admin/",
      "/admin/"
    );
    var doctorPaths = Set.of(
      "/appointment/schedule", // 管理排班
      "/appointment/prescription", // 开具处方
      "/appointment/consult/accept",
      "/appointment/consult/close",
      "/appointment/guidance/"
    );
    var loginPaths = Set.of(
      "/appointment/book",
      "/appointment/cancel/",
      "/appointment/my",
      "/appointment/consult/",
      "/appointment/prescription/list",
      "/appointment/prescription/"
    );
    var residentPaths = Set.of(
      "/appointment/guidance/plan/list",
      "/appointment/prescription/list"
    );
    registry.addInterceptor(new RbacInterceptor(adminPaths, doctorPaths, loginPaths, residentPaths))
      .addPathPatterns("/**");
  }
}
