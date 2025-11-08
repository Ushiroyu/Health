package com.community.health.user.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
    String uid = req.getHeader("X-User-Id");
    if (uid == null || uid.isBlank()) {
      resp.setStatus(401); resp.setContentType("application/json"); resp.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
      return false;
    }
    return true;
  }
}

