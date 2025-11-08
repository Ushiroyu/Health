package com.community.health.activity.controller;

import com.community.health.activity.entity.AuditLog;
import com.community.health.activity.repo.AuditLogRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activity/admin/audit")
public class AdminAuditController {
  private final AuditLogRepository repo;
  public AdminAuditController(AuditLogRepository r){ this.repo = r; }

  @GetMapping
  public ApiResponse<Page<AuditLog>> list(@RequestParam(value = "page", defaultValue = "0") int page,
                                          @RequestParam(value = "size", defaultValue = "20") int size){
    return ApiResponse.ok(repo.findAll(PageRequest.of(page, size)));
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public org.springframework.http.ResponseEntity<String> export(){
    var list = repo.findAll();
    StringBuilder sb = new StringBuilder();
    sb.append("ts,userId,action,targetType,targetId\n");
    for (var a : list) sb.append(a.getTs()).append(',').append(a.getUserId()).append(',').append(a.getAction()).append(',').append(a.getTargetType()).append(',').append(a.getTargetId()).append('\n');
    return org.springframework.http.ResponseEntity.ok().header("Content-Disposition","attachment; filename=activity-audit.csv").body(sb.toString());
  }
}
