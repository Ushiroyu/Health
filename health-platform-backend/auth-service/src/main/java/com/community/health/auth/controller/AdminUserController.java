package com.community.health.auth.controller;

import com.community.health.auth.entity.User;
import com.community.health.auth.repo.UserRepository;
import com.community.health.common.api.ApiResponse;
import com.community.health.common.security.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
  private final UserRepository repo;
  private final com.community.health.auth.repo.AuditLogRepository auditRepo;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  public AdminUserController(UserRepository r, com.community.health.auth.repo.AuditLogRepository a){ this.repo = r; this.auditRepo = a; }

  @GetMapping
  public ApiResponse<?> list(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size){
    return ApiResponse.ok(repo.findAll(org.springframework.data.domain.PageRequest.of(page, size)));
  }

  public record CreateReq(String username, String password, Role role){}
  @PostMapping
  public ApiResponse<Map<String,Object>> create(@RequestHeader("X-User-Id") Long adminId,
                                                @RequestBody CreateReq req){
    if (repo.findByUsername(req.username()).isPresent())
      throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.USERNAME_EXISTS, "user already exists");
    User u = repo.save(User.builder().username(req.username()).password(encoder.encode(req.password())).role(req.role()).build());
    auditRepo.save(com.community.health.auth.entity.AuditLog.builder()
      .ts(java.time.LocalDateTime.now()).userId(adminId).action("CREATE_USER").targetType("USER").targetId(String.valueOf(u.getUserId()))
      .details("role="+req.role()).build());
    return ApiResponse.ok(Map.of("userId", u.getUserId()));
  }

  public record ResetReq(String password){}
  @PostMapping("/{id}/reset-password")
  public ApiResponse<?> reset(@RequestHeader("X-User-Id") Long adminId,
                              @PathVariable("id") Long id, @RequestBody ResetReq req){
    return repo.findById(id).map(u -> { u.setPassword(encoder.encode(req.password())); repo.save(u);
      auditRepo.save(com.community.health.auth.entity.AuditLog.builder().ts(java.time.LocalDateTime.now()).userId(adminId).action("RESET_PASSWORD").targetType("USER").targetId(String.valueOf(id)).build());
      return ApiResponse.ok(); })
      .orElseGet(() -> ApiResponse.error("user not found"));
  }

  @PatchMapping("/{id}/enable")
  public ApiResponse<?> enable(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Long id){
    return setStatus(adminId, id, "ENABLED");
  }
  @PatchMapping("/{id}/disable")
  public ApiResponse<?> disable(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Long id){
    return setStatus(adminId, id, "DISABLED");
  }
  @PatchMapping("/{id}/lock")
  public ApiResponse<?> lock(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Long id){
    return setStatus(adminId, id, "LOCKED");
  }
  @PatchMapping("/{id}/unlock")
  public ApiResponse<?> unlock(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Long id){
    return setStatus(adminId, id, "ENABLED");
  }

  private ApiResponse<?> setStatus(Long adminId, Long id, String status){
    return repo.findById(id).map(u -> { u.setStatus(status); repo.save(u);
      auditRepo.save(com.community.health.auth.entity.AuditLog.builder().ts(java.time.LocalDateTime.now()).userId(adminId).action("SET_STATUS_"+status).targetType("USER").targetId(String.valueOf(id)).build());
      return ApiResponse.ok(); }).orElseGet(() -> ApiResponse.error("user not found"));
  }
}
