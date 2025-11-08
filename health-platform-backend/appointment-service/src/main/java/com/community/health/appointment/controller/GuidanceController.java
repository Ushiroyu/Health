package com.community.health.appointment.controller;

import com.community.health.appointment.entity.GuidancePlan;
import com.community.health.appointment.entity.MeasurementReminder;
import com.community.health.appointment.repo.GuidancePlanRepository;
import com.community.health.appointment.repo.MeasurementReminderRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/appointment")
public class GuidanceController {
  private final GuidancePlanRepository planRepo;
  private final MeasurementReminderRepository reminderRepo;

  public GuidanceController(GuidancePlanRepository planRepo,
                            MeasurementReminderRepository reminderRepo) {
    this.planRepo = planRepo;
    this.reminderRepo = reminderRepo;
  }

  public record PlanReq(Long userId,
                        String type,
                        String rules,
                        String frequency,
                        String dailyTime,
                        String startAt,
                        String endAt) {
  }

  // Only doctors or admins can reach these endpoints (interceptor enforced)
  @PostMapping("/guidance/plan")
  public ApiResponse<Map<String, Object>> createPlan(@RequestBody PlanReq req) {
    GuidancePlan plan = GuidancePlan.builder()
      .userId(req.userId())
      .doctorId(null)
      .type(req.type())
      .rules(req.rules())
      .frequency(req.frequency())
      .dailyTime(req.dailyTime())
      .startAt(req.startAt() == null ? LocalDateTime.now() : parseDateTime(req.startAt()))
      .endAt(parseNullableDateTime(req.endAt()))
      .status("ACTIVE")
      .build();
    GuidancePlan saved = planRepo.save(plan);
    Map<String, Object> payload = new java.util.HashMap<>();
    payload.put("planId", saved.getPlanId());
    return ApiResponse.ok(payload);
  }

  @PutMapping("/guidance/plan/{id}")
  public ApiResponse<?> updatePlan(@PathVariable("id") Long id, @RequestBody PlanReq req) {
    return planRepo.findById(id)
      .map(plan -> {
        plan.setType(req.type());
        plan.setRules(req.rules());
        plan.setFrequency(req.frequency());
        plan.setDailyTime(req.dailyTime());
        if (req.startAt() != null) {
          plan.setStartAt(parseDateTime(req.startAt()));
        }
        plan.setEndAt(parseNullableDateTime(req.endAt()));
        planRepo.save(plan);
        return ApiResponse.ok();
      })
      .orElseGet(this::planNotFound);
  }

  @DeleteMapping("/guidance/plan/{id}")
  public ApiResponse<?> deletePlan(@PathVariable("id") Long id) {
    planRepo.deleteById(id);
    return ApiResponse.ok();
  }

  @GetMapping("/guidance/plan/{id}")
  public ApiResponse<?> getPlan(@PathVariable("id") Long id) {
    return planRepo.findById(id)
      .<ApiResponse<?>>map(ApiResponse::ok)
      .orElseGet(this::planNotFound);
  }

  @GetMapping("/guidance/plan/list")
  public ApiResponse<?> listPlan(@RequestParam("userId") Long userId,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "20") int size) {
    return ApiResponse.ok(planRepo.findByUserIdOrderByStartAtDesc(userId, PageRequest.of(page, size)));
  }

  public record ReminderReq(String type, String dailyTime, Boolean enabled) {
  }

  @PostMapping("/reminder")
  public ApiResponse<Map<String, Object>> createReminder(@RequestHeader("X-User-Id") Long uid,
                                                         @RequestBody ReminderReq req) {
    MeasurementReminder reminder = reminderRepo.save(MeasurementReminder.builder()
      .userId(uid)
      .type(req.type())
      .dailyTime(req.dailyTime())
      .enabled(req.enabled() == null ? true : req.enabled())
      .build());
    return ApiResponse.ok(Map.of("reminderId", reminder.getReminderId()));
  }

  @PutMapping("/reminder/{id}")
  public ApiResponse<?> updateReminder(@RequestHeader("X-User-Id") Long uid,
                                       @PathVariable("id") Long id,
                                       @RequestBody ReminderReq req) {
    return reminderRepo.findById(id)
      .map(reminder -> {
        if (!reminder.getUserId().equals(uid)) {
          return ApiResponse.error("forbidden to modify");
        }
        reminder.setType(req.type());
        reminder.setDailyTime(req.dailyTime());
        if (req.enabled() != null) {
          reminder.setEnabled(req.enabled());
        }
        reminderRepo.save(reminder);
        return ApiResponse.ok();
      })
      .orElseGet(this::reminderNotFound);
  }

  @DeleteMapping("/reminder/{id}")
  public ApiResponse<?> deleteReminder(@RequestHeader("X-User-Id") Long uid,
                                       @PathVariable("id") Long id) {
    return reminderRepo.findById(id)
      .map(reminder -> {
        if (!reminder.getUserId().equals(uid)) {
          return ApiResponse.error("forbidden to delete");
        }
        reminderRepo.delete(reminder);
        return ApiResponse.ok();
      })
      .orElseGet(this::reminderNotFound);
  }

  private ApiResponse<Object> planNotFound() {
    return ApiResponse.error("plan not found");
  }

  private ApiResponse<Object> reminderNotFound() {
    return ApiResponse.error("reminder not found");
  }

  @GetMapping("/reminder/list")
  public ApiResponse<?> listReminder(@RequestHeader("X-User-Id") Long uid) {
    return ApiResponse.ok(reminderRepo.findByUserIdOrderByReminderIdDesc(uid));
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    if (normalized.isEmpty()) {
      return null;
    }
    if (normalized.indexOf('T') < 0 && normalized.indexOf(' ') >= 0) {
      normalized = normalized.replace(' ', 'T');
    }
    return LocalDateTime.parse(normalized);
  }

  private LocalDateTime parseNullableDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return parseDateTime(value);
  }
}
