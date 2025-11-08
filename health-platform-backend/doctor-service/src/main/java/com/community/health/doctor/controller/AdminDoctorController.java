package com.community.health.doctor.controller;

import com.community.health.common.api.ApiResponse;
import com.community.health.doctor.entity.Doctor;
import com.community.health.doctor.repo.DoctorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/doctor")
public class AdminDoctorController {
  private final DoctorRepository repo;
  private final com.community.health.doctor.repo.AuditLogRepository auditRepo;
  private final com.community.health.doctor.repo.DoctorSymptomTagRepository tagRepo;

  public AdminDoctorController(DoctorRepository repo,
                               com.community.health.doctor.repo.AuditLogRepository auditRepo,
                               com.community.health.doctor.repo.DoctorSymptomTagRepository tagRepo) {
    this.repo = repo;
    this.auditRepo = auditRepo;
    this.tagRepo = tagRepo;
  }

  @GetMapping
  public ApiResponse<?> list(@RequestParam(value = "deptId", required = false) Integer deptId,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "q", required = false) String q,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "20") int size) {
    org.springframework.data.jpa.domain.Specification<com.community.health.doctor.entity.Doctor> spec = (root, query, cb) -> {
      java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
      if (deptId != null) {
        predicates.add(cb.equal(root.get("deptId"), deptId));
      }
      if (status != null && !status.isBlank()) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      if (q != null && !q.isBlank()) {
        predicates.add(cb.like(cb.concat(root.get("specialty"), cb.literal(" ")), "%" + q + "%"));
      }
      return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
    var pageable = org.springframework.data.domain.PageRequest.of(page, size);
    return ApiResponse.ok(repo.findAll(spec, pageable));
  }

  @PostMapping
  public ApiResponse<?> create(@RequestHeader("X-User-Id") Long adminId, @RequestBody Doctor doctor) {
    Doctor saved = repo.save(doctor);
    log(adminId, "CREATE_DOCTOR", String.valueOf(saved.getDoctorId()));
    return ApiResponse.ok(saved);
  }

  @PutMapping("/{id}")
  public ApiResponse<?> update(@RequestHeader("X-User-Id") Long adminId,
                               @PathVariable("id") Long id,
                               @RequestBody Doctor doctor) {
    doctor.setDoctorId(id);
    Doctor saved = repo.save(doctor);
    log(adminId, "UPDATE_DOCTOR", String.valueOf(id));
    return ApiResponse.ok(saved);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<?> delete(@RequestHeader("X-User-Id") Long adminId,
                               @PathVariable("id") Long id) {
    repo.deleteById(id);
    log(adminId, "DELETE_DOCTOR", String.valueOf(id));
    return ApiResponse.ok();
  }

  @PatchMapping("/{id}/enable")
  public ApiResponse<?> enable(@RequestHeader("X-User-Id") Long adminId,
                               @PathVariable("id") Long id) {
    return setStatus(adminId, id, "ENABLED");
  }

  @PatchMapping("/{id}/disable")
  public ApiResponse<?> disable(@RequestHeader("X-User-Id") Long adminId,
                                @PathVariable("id") Long id) {
    return setStatus(adminId, id, "DISABLED");
  }

  @PostMapping("/{id}/approve")
  public ApiResponse<?> approve(@RequestHeader("X-User-Id") Long adminId,
                                @PathVariable("id") Long id) {
    return setStatus(adminId, id, "ENABLED");
  }

  @PostMapping("/{id}/reject")
  public ApiResponse<?> reject(@RequestHeader("X-User-Id") Long adminId,
                               @PathVariable("id") Long id) {
    return setStatus(adminId, id, "DISABLED");
  }

  @GetMapping("/{id}/tags")
  public ApiResponse<?> listTags(@PathVariable("id") Long id) {
    List<String> tags = tagRepo.findByDoctorId(id).stream()
      .map(com.community.health.doctor.entity.DoctorSymptomTag::getTag)
      .toList();
    return ApiResponse.ok(tags);
  }

  public record TagUpdateReq(List<String> tags) {
  }

  @PutMapping("/{id}/tags")
  public ApiResponse<?> updateTags(@RequestHeader("X-User-Id") Long adminId,
                                   @PathVariable("id") Long id,
                                   @RequestBody TagUpdateReq req) {
    List<String> tags = req.tags() == null ? List.of() : req.tags().stream()
      .filter(tag -> tag != null && !tag.isBlank())
      .map(String::trim)
      .map(String::toLowerCase)
      .distinct()
      .toList();
    tagRepo.deleteByDoctorId(id);
    for (String tag : tags) {
      tagRepo.save(com.community.health.doctor.entity.DoctorSymptomTag.builder()
        .doctorId(id)
        .tag(tag)
        .build());
    }
    log(adminId, "UPDATE_TAGS", String.valueOf(id));
    return ApiResponse.ok();
  }

  private ApiResponse<?> setStatus(Long adminId, Long id, String status) {
    return repo.findById(id)
      .map(doctor -> {
        doctor.setStatus(status);
        repo.save(doctor);
        log(adminId, "SET_STATUS_" + status, String.valueOf(id));
        return ApiResponse.ok();
      })
      .orElseGet(() -> ApiResponse.error("Doctor not found"));
  }

  private void log(Long userId, String action, String targetId) {
    auditRepo.save(com.community.health.doctor.entity.AuditLog.builder()
      .ts(java.time.LocalDateTime.now())
      .userId(userId)
      .action(action)
      .targetType("DOCTOR")
      .targetId(targetId)
      .build());
  }
}
