package com.community.health.activity.controller;

import com.community.health.activity.entity.CommunityEvent;
import com.community.health.activity.repo.CommunityEventRepository;
import com.community.health.activity.repo.AuditLogRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/activity/admin")
public class AdminActivityController {
  private final CommunityEventRepository repo;
  private final AuditLogRepository auditRepo;
  private final Optional<com.community.health.activity.mq.ActivityEventPublisher> eventPublisher;
  public AdminActivityController(CommunityEventRepository r,
                                 AuditLogRepository a,
                                 Optional<com.community.health.activity.mq.ActivityEventPublisher> publisher){
    this.repo = r; this.auditRepo = a; this.eventPublisher = publisher;
  }

  @PostMapping("/event")
  public ApiResponse<?> create(@RequestHeader("X-User-Id") Long adminId, @RequestBody CommunityEvent e){ e.setStatus("DRAFT"); var saved = repo.save(e); audit(adminId, "CREATE_EVENT", saved.getEventId()); return ApiResponse.ok(saved); }
  @PutMapping("/event/{id}")
  public ApiResponse<?> update(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Long id, @RequestBody CommunityEvent e){ e.setEventId(id); var saved = repo.save(e); audit(adminId, "UPDATE_EVENT", id); return ApiResponse.ok(saved); }
  @DeleteMapping("/event/{id}")
  public ApiResponse<?> delete(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Long id){ repo.deleteById(id); audit(adminId, "DELETE_EVENT", id); return ApiResponse.ok(); }
  @PostMapping("/event/{id}/publish")
  public ApiResponse<?> publish(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Long id){
    return repo.findById(id).map(e -> {
      e.setStatus("PUBLISHED");
      audit(adminId, "PUBLISH_EVENT", id);
      var saved = repo.save(e);
      eventPublisher.ifPresent(p -> p.publishEvent(saved));
      return ApiResponse.ok(saved);
    }).orElseGet(() -> ApiResponse.error("活动不存在"));
  }
  @PostMapping("/event/{id}/unpublish")
  public ApiResponse<?> unpublish(@RequestHeader("X-User-Id") Long adminId, @PathVariable("id") Long id){
    return repo.findById(id).map(e -> {
      e.setStatus("DRAFT");
      audit(adminId, "UNPUBLISH_EVENT", id);
      var saved = repo.save(e);
      eventPublisher.ifPresent(p -> p.publishEvent(saved));
      return ApiResponse.ok(saved);
    }).orElseGet(() -> ApiResponse.error("活动不存在"));
  }

  private void audit(Long uid, String action, Object targetId){
    auditRepo.save(com.community.health.activity.entity.AuditLog.builder().ts(LocalDateTime.now()).userId(uid).action(action).targetType("EVENT").targetId(String.valueOf(targetId)).build());
  }
}
