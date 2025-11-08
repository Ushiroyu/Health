package com.community.health.auth.controller;

import com.community.health.auth.service.AuthService;
import com.community.health.common.api.ApiResponse;
import com.community.health.common.security.Role;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService s){ this.authService = s; }

  public record LoginReq(String username, String password){}
  public record RegReq(String username, String password, Role role){}

  @PostMapping("/register")
  public ApiResponse<Map<String,Object>> register(@RequestBody RegReq req){
    return authService.register(req.username(), req.password(), req.role());
  }

  @PostMapping("/login")
  public ApiResponse<Map<String,Object>> login(@RequestBody LoginReq req){
    return authService.login(req.username(), req.password());
  }
}
