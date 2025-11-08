package com.community.health.doctor.controller;

import com.community.health.common.api.ApiResponse;
import com.community.health.doctor.repo.DepartmentRepository;
import com.community.health.doctor.repo.DoctorRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DoctorController {
  private final DepartmentRepository deptRepo;
  private final DoctorRepository docRepo;
  private final com.community.health.doctor.repo.DoctorSymptomTagRepository tagRepo;
  private final com.community.health.doctor.repo.ScheduleSlotRepository scheduleRepo;
  public DoctorController(DepartmentRepository d,
                          DoctorRepository r,
                          com.community.health.doctor.repo.DoctorSymptomTagRepository t,
                          com.community.health.doctor.repo.ScheduleSlotRepository s){
    this.deptRepo = d; this.docRepo = r; this.tagRepo = t; this.scheduleRepo = s;
  }

  @GetMapping("/department/list")
  public ApiResponse<?> depts(){ return ApiResponse.ok(deptRepo.findAll()); }

  @GetMapping("/doctor/list")
  public ApiResponse<?> doctors(@RequestParam(value = "deptId", required=false) Integer deptId,
                                @RequestParam(value = "q", required=false) String q){
    if (deptId!=null) return ApiResponse.ok(docRepo.findByDeptId(deptId));
    if (q!=null && !q.isBlank()) return ApiResponse.ok(docRepo.searchByNameOrSpecialty("%"+q+"%"));
    return ApiResponse.ok(docRepo.findAll());
  }

  @GetMapping("/doctor/search")
  public ApiResponse<?> search(@RequestParam(value = "deptId", required = false) Integer deptId,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "symptoms", required = false) String symptoms,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "date", required = false) String date,
                               @RequestParam(value = "time", required = false) String time,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size) {
    var spec = (org.springframework.data.jpa.domain.Specification<com.community.health.doctor.entity.Doctor>) (root, query, cb) -> {
      List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
      if (deptId != null) {
        predicates.add(cb.equal(root.get("deptId"), deptId));
      }
      if (keyword != null && !keyword.isBlank()) {
        String like = "%" + keyword.trim() + "%";
        predicates.add(cb.or(
          cb.like(cb.lower(root.get("specialty")), like.toLowerCase(Locale.ROOT)),
          cb.like(cb.lower(root.get("title")), like.toLowerCase(Locale.ROOT))
        ));
      }
      if (status != null && !status.isBlank()) {
        predicates.add(cb.equal(root.get("status"), status));
      } else {
        predicates.add(cb.or(
          cb.isNull(root.get("status")),
          cb.equal(root.get("status"), "ENABLED")
        ));
      }
      List<String> symptomFilters = parseSymptoms(symptoms);
      if (!symptomFilters.isEmpty()) {
        var sub = query.subquery(Long.class);
        var tagRoot = sub.from(com.community.health.doctor.entity.DoctorSymptomTag.class);
        sub.select(tagRoot.get("doctorId"))
          .where(
            cb.equal(tagRoot.get("doctorId"), root.get("doctorId")),
            tagRoot.get("tag").in(symptomFilters)
          );
        predicates.add(cb.exists(sub));
      }
      return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };

    var pageable = org.springframework.data.domain.PageRequest.of(page, size);
    var resultPage = docRepo.findAll(spec, pageable);
    if (resultPage.isEmpty()) {
      return ApiResponse.ok(Map.of(
        "content", List.of(),
        "page", resultPage.getNumber(),
        "size", resultPage.getSize(),
        "totalElements", resultPage.getTotalElements(),
        "totalPages", resultPage.getTotalPages()
      ));
    }

    List<Long> doctorIds = resultPage.stream().map(com.community.health.doctor.entity.Doctor::getDoctorId).toList();
    Map<Long, List<String>> tagMap = tagRepo.findByDoctorIdIn(doctorIds).stream()
      .collect(Collectors.groupingBy(com.community.health.doctor.entity.DoctorSymptomTag::getDoctorId,
        Collectors.mapping(com.community.health.doctor.entity.DoctorSymptomTag::getTag, Collectors.toList())));

    LocalDate targetDate = date != null && !date.isBlank() ? LocalDate.parse(date) : null;
    Map<Long, List<String>> slotMap = new LinkedHashMap<>();
    Map<Long, Boolean> availability = new LinkedHashMap<>();
    doctorIds.forEach(id -> availability.put(id, Boolean.FALSE));

    if (targetDate != null) {
      Collection<com.community.health.doctor.entity.ScheduleSlot> slots;
      if (time != null && !time.isBlank()) {
        slots = scheduleRepo.findByDoctorIdInAndDateAndTimeSlot(doctorIds, targetDate, time);
      } else {
        slots = scheduleRepo.findByDoctorIdInAndDate(doctorIds, targetDate);
      }
      for (var slot : slots) {
        if (slot == null) continue;
        boolean open = slot.getStatus() == null || slot.getStatus().equalsIgnoreCase("OPEN");
        boolean hasCapacity = slot.getCapacity() == null || slot.getCapacity() > 0;
        if (open && hasCapacity) {
          availability.put(slot.getDoctorId(), Boolean.TRUE);
          if (time == null || time.isBlank()) {
            slotMap.computeIfAbsent(slot.getDoctorId(), k -> new ArrayList<>()).add(slot.getTimeSlot());
          }
        }
      }
    }

    List<Map<String, Object>> content = resultPage.stream().map(doc -> {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("doctorId", doc.getDoctorId());
      row.put("deptId", doc.getDeptId());
      row.put("title", doc.getTitle());
      row.put("specialty", doc.getSpecialty());
      row.put("status", doc.getStatus());
      row.put("tags", tagMap.getOrDefault(doc.getDoctorId(), List.of()));
      if (targetDate != null) {
        row.put("date", targetDate.toString());
        row.put("available", availability.getOrDefault(doc.getDoctorId(), Boolean.FALSE));
        if (time == null || time.isBlank()) {
          row.put("availableSlots", slotMap.getOrDefault(doc.getDoctorId(), List.of()));
        } else {
          row.put("timeSlot", time);
        }
      }
      return row;
    }).toList();

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("content", content);
    payload.put("page", resultPage.getNumber());
    payload.put("size", resultPage.getSize());
    payload.put("totalElements", resultPage.getTotalElements());
    payload.put("totalPages", resultPage.getTotalPages());
    return ApiResponse.ok(payload);
  }

  private List<String> parseSymptoms(String symptoms) {
    if (symptoms == null || symptoms.isBlank()) return List.of();
    return java.util.Arrays.stream(symptoms.split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .map(String::toLowerCase)
      .distinct()
      .toList();
  }
}
