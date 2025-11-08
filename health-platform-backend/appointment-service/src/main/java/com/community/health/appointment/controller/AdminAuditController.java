package com.community.health.appointment.controller;

import com.community.health.appointment.entity.AuditLog;
import com.community.health.appointment.repo.AuditLogRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping({"/admin/audit", "/appointment/admin/audit"})
public class AdminAuditController {
  private final AuditLogRepository repo;
  public AdminAuditController(AuditLogRepository r){ this.repo = r; }

  @GetMapping
  public ApiResponse<Page<AuditLog>> list(@RequestParam(value = "from", required = false) String from,
                                          @RequestParam(value = "to", required = false) String to,
                                          @RequestParam(value = "userId", required = false) Long userId,
                                          @RequestParam(value = "action", required = false) String action,
                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                          @RequestParam(value = "size", defaultValue = "20") int size){
    Specification<AuditLog> spec = (root, q, cb) -> {
      var ps = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
      if (from != null && !from.isBlank()) ps.add(cb.greaterThanOrEqualTo(root.get("ts"), LocalDateTime.parse(from)));
      if (to != null && !to.isBlank()) ps.add(cb.lessThanOrEqualTo(root.get("ts"), LocalDateTime.parse(to)));
      if (userId != null) ps.add(cb.equal(root.get("userId"), userId));
      if (action != null && !action.isBlank()) ps.add(cb.equal(root.get("action"), action));
      return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
    return ApiResponse.ok(repo.findAll(spec, PageRequest.of(page, size)));
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public org.springframework.http.ResponseEntity<String> export(@RequestParam(value = "from", required = false) String from,
                                                               @RequestParam(value = "to", required = false) String to,
                                                               @RequestParam(value = "userId", required = false) Long userId,
                                                               @RequestParam(value = "action", required = false) String action) {
    var spec = (Specification<AuditLog>) (root, q, cb) -> {
      var ps = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
      if (from != null && !from.isBlank()) ps.add(cb.greaterThanOrEqualTo(root.get("ts"), LocalDateTime.parse(from)));
      if (to != null && !to.isBlank()) ps.add(cb.lessThanOrEqualTo(root.get("ts"), LocalDateTime.parse(to)));
      if (userId != null) ps.add(cb.equal(root.get("userId"), userId));
      if (action != null && !action.isBlank()) ps.add(cb.equal(root.get("action"), action));
      return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
    var list = repo.findAll(spec);
    StringBuilder sb = new StringBuilder();
    sb.append("ts,userId,action,targetType,targetId\n");
    for (var a : list) {
      sb.append(a.getTs()).append(',').append(a.getUserId()).append(',').append(a.getAction()).append(',').append(a.getTargetType()).append(',').append(a.getTargetId()).append('\n');
    }
    return org.springframework.http.ResponseEntity.ok()
      .header("Content-Disposition", "attachment; filename=appointment-audit.csv")
      .body(sb.toString());
  }
}
