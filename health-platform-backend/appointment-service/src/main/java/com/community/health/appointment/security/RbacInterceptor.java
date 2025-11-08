package com.community.health.appointment.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

public class RbacInterceptor implements HandlerInterceptor {
  private final Set<String> adminPaths;
  private final Set<String> doctorOnlyPaths;
  private final Set<String> loginPaths;
  private final Set<String> residentPaths;

  public RbacInterceptor(Set<String> adminPaths,
                         Set<String> doctorOnlyPaths,
                         Set<String> loginPaths,
                         Set<String> residentPaths) {
    this.adminPaths = adminPaths;
    this.doctorOnlyPaths = doctorOnlyPaths;
    this.loginPaths = loginPaths;
    this.residentPaths = residentPaths;
  }

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
    String path = req.getRequestURI();
    String userId = req.getHeader("X-User-Id");
    String role = req.getHeader("X-User-Role");

    if (path.startsWith("/appointment/schedule") && "GET".equalsIgnoreCase(req.getMethod())) {
      if (!isLoggedIn(userId)) return unauthorized(resp);
      return true;
    }

    // simple matcher by prefix
    if (matches(path, adminPaths)) {
      if (!isLoggedIn(userId)) return unauthorized(resp);
      if (!isRole(role, "ADMIN")) return forbidden(resp);
      return true;
    }
    if (matches(path, residentPaths)) {
      if (!isLoggedIn(userId)) return unauthorized(resp);
      return true;
    }
    if (matches(path, doctorOnlyPaths)) {
      if (!isLoggedIn(userId)) return unauthorized(resp);
      if (!(isRole(role, "DOCTOR") || isRole(role, "ADMIN"))) return forbidden(resp);
      return true;
    }
    if (matches(path, loginPaths)) {
      if (!isLoggedIn(userId)) return unauthorized(resp);
      return true;
    }
    return true;
  }

  private boolean matches(String path, Set<String> prefixes){
    if (prefixes == null) return false;
    for (String p : prefixes) if (path.startsWith(p)) return true;
    return false;
  }
  private boolean isLoggedIn(String uid){ return uid != null && !uid.isBlank(); }
  private boolean isRole(String role, String expected){ return expected.equalsIgnoreCase(role); }
  private boolean unauthorized(HttpServletResponse resp) throws Exception { resp.setStatus(401); resp.setContentType("application/json"); resp.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}"); return false; }
  private boolean forbidden(HttpServletResponse resp) throws Exception { resp.setStatus(403); resp.setContentType("application/json"); resp.getWriter().write("{\"code\":403,\"message\":\"Forbidden\"}"); return false; }
}
