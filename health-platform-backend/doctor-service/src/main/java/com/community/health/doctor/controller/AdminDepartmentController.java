package com.community.health.doctor.controller;

import com.community.health.common.api.ApiResponse;
import com.community.health.doctor.entity.Department;
import com.community.health.doctor.repo.DepartmentRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/department")
public class AdminDepartmentController {
  private final DepartmentRepository repo;

  @GetMapping
  public ApiResponse<?> list(){ return ApiResponse.ok(repo.findAll()); }

  @PostMapping
  public ApiResponse<?> create(@RequestHeader("X-User-Id") Long adminId, @RequestBody Department d){
    var saved = repo.save(d);
    log(adminId, "CREATE_DEPT", String.valueOf(saved.getDeptId()), null);
    return ApiResponse.ok(saved);
  }

  @PutMapping("/{id}")
  public ApiResponse<?> update(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Integer id, @RequestBody Department d){
    d.setDeptId(id);
    var saved = repo.save(d);
    log(adminId, "UPDATE_DEPT", String.valueOf(id), null);
    return ApiResponse.ok(saved);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<?> delete(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Integer id){ repo.deleteById(id); log(adminId, "DELETE_DEPT", String.valueOf(id), null); return ApiResponse.ok(); }

  private final com.community.health.doctor.repo.AuditLogRepository auditRepo;
  public AdminDepartmentController(DepartmentRepository r, com.community.health.doctor.repo.AuditLogRepository a){ this.repo = r; this.auditRepo = a; }
  private void log(Long uid, String action, String targetId, String details){
    auditRepo.save(com.community.health.doctor.entity.AuditLog.builder().ts(java.time.LocalDateTime.now()).userId(uid).action(action).targetType("DEPT").targetId(targetId).details(details).build());
  }
}
