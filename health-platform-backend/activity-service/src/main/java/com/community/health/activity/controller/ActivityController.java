package com.community.health.activity.controller;

import com.community.health.activity.entity.CommunityEvent;
import com.community.health.activity.entity.EventRegistration;
import com.community.health.activity.repo.AuditLogRepository;
import com.community.health.activity.repo.CommunityEventRepository;
import com.community.health.activity.repo.EventRegistrationRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/activity")
public class ActivityController {
  private final CommunityEventRepository eventRepo;
  private final EventRegistrationRepository regRepo;
  private final AuditLogRepository auditRepo;
  public ActivityController(CommunityEventRepository e, EventRegistrationRepository r, AuditLogRepository a){ this.eventRepo = e; this.regRepo = r; this.auditRepo = a; }

  @PostMapping("/event/save")
  public ApiResponse<?> saveEvent(@RequestBody CommunityEvent e){ return ApiResponse.ok(eventRepo.save(e)); }

  @GetMapping("/event/list")
  public ApiResponse<?> listEvent(){ return ApiResponse.ok(eventRepo.findAll()); }

  @PostMapping("/event/{id}/register")
  public ApiResponse<?> register(@RequestHeader("X-User-Id") Long uid, @PathVariable("id") Long id){
    CommunityEvent ev = eventRepo.findById(id).orElse(null);
    if (ev == null) throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.ACTIVITY_NOT_FOUND, "活动不存在");
    if (ev.getCapacity() != null && ev.getCapacity() > 0) {
      long cnt = regRepo.countByEventId(id);
      if (cnt >= ev.getCapacity()) throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.ACTIVITY_FULL, "名额已满");
    }
    if (ev.getEventDate() != null && ev.getEventDate().isBefore(LocalDateTime.now())) {
      throw new com.community.health.common.exception.BusinessException(com.community.health.common.exception.ErrorCodes.ACTIVITY_STARTED, "活动已开始/结束");
    }
    EventRegistration reg = regRepo.save(EventRegistration.builder().eventId(id).userId(uid).regTime(LocalDateTime.now()).build());
    auditRepo.save(com.community.health.activity.entity.AuditLog.builder().ts(LocalDateTime.now()).userId(uid).action("REGISTER_EVENT").targetType("EVENT").targetId(String.valueOf(id)).build());
    return ApiResponse.ok(Map.of("regId", reg.getRegId()));
  }

  @GetMapping("/event/search")
  public ApiResponse<?> search(@RequestParam(value = "q", required = false) String q,
                               @RequestParam(value = "from", required = false) String from,
                               @RequestParam(value = "to", required = false) String to,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size){
    var p = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("eventDate").descending());
    if (q != null && !q.isBlank()) return ApiResponse.ok(eventRepo.findByTitleContainingIgnoreCase(q, p));
    if (from != null || to != null){
      var spec = (org.springframework.data.jpa.domain.Specification<com.community.health.activity.entity.CommunityEvent>)(root, query, cb) -> {
        java.util.List<jakarta.persistence.criteria.Predicate> ps = new java.util.ArrayList<>();
        if (from != null && !from.isBlank()) ps.add(cb.greaterThanOrEqualTo(root.get("eventDate"), java.time.LocalDateTime.parse(from)));
        if (to != null && !to.isBlank()) ps.add(cb.lessThanOrEqualTo(root.get("eventDate"), java.time.LocalDateTime.parse(to)));
        return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
      };
      return ApiResponse.ok(eventRepo.findAll(spec, p));
    }
    return ApiResponse.ok(eventRepo.findAll(p));
  }

  @DeleteMapping("/event/{id}/register")
  public ApiResponse<?> cancelRegister(@RequestHeader("X-User-Id") Long uid, @PathVariable("id") Long id){
    return regRepo.findByEventIdAndUserId(id, uid)
      .map(r -> { regRepo.delete(r); auditRepo.save(com.community.health.activity.entity.AuditLog.builder().ts(LocalDateTime.now()).userId(uid).action("CANCEL_REG_EVENT").targetType("EVENT").targetId(String.valueOf(id)).build()); return ApiResponse.ok(); })
      .orElseGet(() -> new ApiResponse<>(com.community.health.common.exception.ErrorCodes.ACTIVITY_REG_NOT_FOUND, "未报名该活动", null));
  }

  @GetMapping("/my-registrations")
  public ApiResponse<Page<EventRegistration>> myRegs(@RequestHeader("X-User-Id") Long uid,
                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "20") int size) {
    return ApiResponse.ok(regRepo.findByUserIdOrderByRegTimeDesc(uid, PageRequest.of(page, size)));
  }
}
