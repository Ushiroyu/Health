package com.community.health.user.controller;

import com.community.health.common.api.ApiResponse;
import com.community.health.common.exception.BusinessException;
import com.community.health.common.exception.ErrorCodes;
import com.community.health.user.entity.HealthProfile;
import com.community.health.user.repo.HealthProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user/profile")
public class HealthProfileController {

  private final HealthProfileRepository repo;

  public HealthProfileController(HealthProfileRepository repo) {
    this.repo = repo;
  }

  public record ProfileUpdateReq(
      String fullName,
      String gender,
      String birthDate,
      String bloodType,
      String phone,
      String email,
      String address,
      String idNumber,
      String chronicConditions,
      String allergies,
      String medicalHistory,
      String medications,
      String emergencyContact,
      String emergencyPhone,
      String lifestyleNotes
  ) { }

  @GetMapping("/me")
  public ApiResponse<HealthProfile> myProfile(@RequestHeader("X-User-Id") Long userId) {
    return ApiResponse.ok(repo.findByUserId(userId).orElseGet(() ->
      HealthProfile.builder().userId(userId).build()
    ));
  }

  @PutMapping("/me")
  public ApiResponse<Map<String, Object>> updateMyProfile(@RequestHeader("X-User-Id") Long userId,
                                                          @RequestBody ProfileUpdateReq req) {
    HealthProfile profile = repo.findByUserId(userId).orElseGet(() ->
      HealthProfile.builder().userId(userId).build()
    );
    apply(profile, req);
    HealthProfile saved = repo.save(profile);
    Map<String, Object> payload = new HashMap<>();
    payload.put("profileId", saved.getProfileId());
    payload.put("lastUpdated", saved.getLastUpdated());
    return ApiResponse.ok(payload);
  }

  @GetMapping("/{targetUserId}")
  public ApiResponse<HealthProfile> viewProfile(@RequestHeader("X-User-Role") Optional<String> role,
                                                @PathVariable("targetUserId") Long targetUserId) {
    ensureDoctorOrAdmin(role.orElse(null));
    return repo.findByUserId(targetUserId)
      .map(ApiResponse::ok)
      .orElseGet(() -> new ApiResponse<>(ErrorCodes.NOT_FOUND, "档案不存在", null));
  }

  @GetMapping("/search")
  public ApiResponse<Page<HealthProfile>> search(@RequestHeader("X-User-Role") Optional<String> role,
                                                 @RequestParam(value = "keyword", required = false) String keyword,
                                                 @RequestParam(value = "chronicCondition", required = false) String chronicCondition,
                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "size", defaultValue = "20") int size) {
    ensureAdmin(role.orElse(null));
    Specification<HealthProfile> spec = (root, query, cb) -> {
      var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
      if (StringUtils.hasText(keyword)) {
        String like = "%" + keyword.trim() + "%";
        predicates.add(cb.or(
          cb.like(cb.lower(root.get("fullName")), like.toLowerCase()),
          cb.like(cb.lower(root.get("chronicConditions")), like.toLowerCase()),
          cb.like(cb.lower(root.get("allergies")), like.toLowerCase()),
          cb.like(cb.lower(root.get("medicalHistory")), like.toLowerCase())
        ));
      }
      if (StringUtils.hasText(chronicCondition)) {
        String like = "%" + chronicCondition.trim() + "%";
        predicates.add(cb.like(cb.lower(root.get("chronicConditions")), like.toLowerCase()));
      }
      if (predicates.isEmpty()) {
        return cb.conjunction();
      }
      return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
    Pageable pageable = PageRequest.of(page, size);
    return ApiResponse.ok(repo.findAll(spec, pageable));
  }

  private void apply(HealthProfile profile, ProfileUpdateReq req) {
    profile.setFullName(req.fullName());
    profile.setGender(req.gender());
    profile.setBloodType(req.bloodType());
    if (StringUtils.hasText(req.birthDate())) {
      profile.setBirthDate(LocalDate.parse(req.birthDate()));
    } else {
      profile.setBirthDate(null);
    }
    profile.setPhone(req.phone());
    profile.setEmail(req.email());
    profile.setAddress(req.address());
    profile.setIdNumber(req.idNumber());
    profile.setChronicConditions(req.chronicConditions());
    profile.setAllergies(req.allergies());
    profile.setMedicalHistory(req.medicalHistory());
    profile.setMedications(req.medications());
    profile.setEmergencyContact(req.emergencyContact());
    profile.setEmergencyPhone(req.emergencyPhone());
    profile.setLifestyleNotes(req.lifestyleNotes());
  }

  private void ensureDoctorOrAdmin(String role) {
    if (role == null || !(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("DOCTOR"))) {
      throw new BusinessException(ErrorCodes.FORBIDDEN, "仅医生或管理员可查看他人档案");
    }
  }

  private void ensureAdmin(String role) {
    if (role == null || !role.equalsIgnoreCase("ADMIN")) {
      throw new BusinessException(ErrorCodes.FORBIDDEN, "仅管理员可执行该操作");
    }
  }
}
