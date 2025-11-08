package com.community.health.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  static class AdminInterceptor implements HandlerInterceptor {
    @Override public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object h) throws Exception {
      String path = req.getRequestURI();
      if (!path.startsWith("/admin/")) return true;
      String role = req.getHeader("X-User-Role");
      if (role == null || !role.equalsIgnoreCase("ADMIN")) { resp.setStatus(403); resp.setContentType("application/json"); resp.getWriter().write("{\"code\":403,\"message\":\"Forbidden\"}"); return false; }
      return true;
    }
  }
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new AdminInterceptor()).addPathPatterns("/**");
  }
}

