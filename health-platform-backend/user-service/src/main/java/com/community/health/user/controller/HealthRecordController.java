package com.community.health.user.controller;

import com.community.health.common.api.ApiResponse;
import com.community.health.user.entity.HealthRecord;
import com.community.health.user.repo.HealthRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/record")
public class HealthRecordController {
  private final HealthRecordRepository repo;
  public HealthRecordController(HealthRecordRepository r){ this.repo = r; }

  public record SaveReq(String type, String value, String note, String date) {}

  @PostMapping("/save")
  public ApiResponse<Map<String, Object>> save(@RequestHeader("X-User-Id") Long uid,
                                               @RequestBody SaveReq req) {
    HealthRecord hr = repo.save(HealthRecord.builder()
      .userId(uid)
      .type(req.type())
      .value(req.value())
      .note(req.note())
      .recordDate(req.date()==null? LocalDate.now(): LocalDate.parse(req.date()))
      .build());
    return ApiResponse.ok(Map.of("recordId", hr.getRecordId()));
  }

  @GetMapping("/list")
  public ApiResponse<?> list(@RequestHeader("X-User-Id") Long uid,
                             @RequestParam("type") String type,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "20") int size) {
    return ApiResponse.ok(repo.findByUserIdAndTypeOrderByRecordDateDesc(uid, type, PageRequest.of(page, size)));
  }

  @GetMapping("/trend")
  public ApiResponse<?> trend(@RequestHeader("X-User-Id") Long uid,
                              @RequestParam("type") String type,
                              @RequestParam("from") String from,
                              @RequestParam("to") String to) {
    LocalDate f = LocalDate.parse(from);
    LocalDate t = LocalDate.parse(to);
    List<HealthRecord> list = repo
      .findByUserIdAndTypeOrderByRecordDateDesc(uid, type, PageRequest.of(0, Integer.MAX_VALUE))
      .getContent()
      .stream()
      .filter(r -> !r.getRecordDate().isBefore(f) && !r.getRecordDate().isAfter(t))
      .sorted((a,b) -> a.getRecordDate().compareTo(b.getRecordDate()))
      .collect(Collectors.toList());

    // 将同一天的多条做简单平均，返回 [{date:yyyy-MM-dd, value:avg}]
    Map<LocalDate, List<Double>> grouped = list.stream().collect(Collectors.groupingBy(
      HealthRecord::getRecordDate,
      LinkedHashMap::new,
      Collectors.mapping(r -> parseValue(r.getValue()), Collectors.toList())
    ));
    List<Map<String,Object>> series = grouped.entrySet().stream().map(e -> {
      double avg = e.getValue().stream().filter(v -> v != null).mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
      java.util.HashMap<String,Object> m = new java.util.HashMap<>();
      m.put("date", e.getKey().toString());
      m.put("value", Double.valueOf(avg));
      return m;
    }).collect(Collectors.toList());
    java.util.HashMap<String,Object> out = new java.util.HashMap<>();
    out.put("series", series);
    return ApiResponse.ok(out);
  }

  private Double parseValue(String v) {
    if (v == null) return null;
    // 支持血压 "120/80" 取收缩压作为趋势值（演示），后续可返回双曲线
    if (v.contains("/")) {
      String s = v.split("/")[0];
      try { return Double.parseDouble(s); } catch (Exception ignore) { return null; }
    }
    try { return Double.parseDouble(v); } catch (Exception ignore) { return null; }
  }
}
