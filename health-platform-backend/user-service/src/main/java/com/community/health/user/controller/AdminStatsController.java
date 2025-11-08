package com.community.health.user.controller;

import com.community.health.common.api.ApiResponse;
import com.community.health.user.entity.HealthRecord;
import com.community.health.user.repo.HealthRecordRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {
  private final HealthRecordRepository repo;
  public AdminStatsController(HealthRecordRepository r){ this.repo = r; }

  @GetMapping("/health-records")
  public ApiResponse<?> records(@RequestParam("type") String type,
                                @RequestParam("from") String from,
                                @RequestParam("to") String to,
                                @RequestParam(value = "bucket", defaultValue = "day") String bucket) {
    LocalDate f = LocalDate.parse(from);
    LocalDate t = LocalDate.parse(to);
    Map<String, Long> buckets = new LinkedHashMap<>();
    LocalDate cur = f;
    while (!cur.isAfter(t)) { buckets.put(formatBucket(cur, bucket), 0L); cur = nextBucket(cur, bucket); }
    var daily = repo.aggregateByDate(type, f, t);
    for (var d : daily) {
      String key = formatBucket(d.getD(), bucket);
      buckets.computeIfPresent(key, (k,v) -> v + (d.getTotal()==null?0:d.getTotal()));
    }
    List<Map<String,Object>> series = new ArrayList<>();
    for (var e : buckets.entrySet()) series.add(Map.of("bucket", e.getKey(), "count", e.getValue()));
    return ApiResponse.ok(series);
  }

  @GetMapping(value = "/health-records/export", produces = "text/csv;charset=UTF-8")
  public org.springframework.http.ResponseEntity<String> exportRecords(@RequestParam("type") String type,
                                                                       @RequestParam("from") String from,
                                                                       @RequestParam("to") String to,
                                                                       @RequestParam(value = "bucket", defaultValue = "day") String bucket) {
    var res = (java.util.List<java.util.Map<String,Object>>) records(type, from, to, bucket).data();
    StringBuilder sb = new StringBuilder();
    sb.append("bucket,count\n");
    for (var m : res) sb.append(m.get("bucket")).append(',').append(m.get("count")).append('\n');
    return org.springframework.http.ResponseEntity.ok().header("Content-Disposition","attachment; filename=health-records-agg.csv").body(sb.toString());
  }

  @GetMapping("/abnormal-rate")
  public ApiResponse<?> abnormalRate(@RequestParam("type") String type,
                                     @RequestParam("from") String from,
                                     @RequestParam("to") String to,
                                     @RequestParam(value = "bucket", defaultValue = "day") String bucket) {
    LocalDate f = LocalDate.parse(from);
    LocalDate t = LocalDate.parse(to);
    Map<String, long[]> buckets = new LinkedHashMap<>(); // [total, abnormal]
    LocalDate cur = f;
    while (!cur.isAfter(t)) {
      String key = formatBucket(cur, bucket);
      buckets.putIfAbsent(key, new long[]{0,0});
      cur = nextBucket(cur, bucket);
    }
    var list = repo.findByTypeAndRecordDateBetween(type, f, t);
    for (HealthRecord hr : list) {
      String key = formatBucket(hr.getRecordDate(), bucket);
      long[] arr = buckets.get(key); if (arr==null) continue;
      arr[0]++;
      if (isAbnormal(type, hr.getValue())) arr[1]++;
    }
    List<Map<String,Object>> series = new ArrayList<>();
    for (var e : buckets.entrySet()) {
      long total = e.getValue()[0], abn = e.getValue()[1];
      double rate = total==0? 0.0 : (abn*1.0/total);
      series.add(Map.of("bucket", e.getKey(), "total", total, "abnormal", abn, "rate", rate));
    }
    return ApiResponse.ok(series);
  }

  @GetMapping(value = "/abnormal-rate/export", produces = "text/csv;charset=UTF-8")
  public org.springframework.http.ResponseEntity<String> exportAbnormal(@RequestParam("type") String type,
                                                                        @RequestParam("from") String from,
                                                                        @RequestParam("to") String to,
                                                                        @RequestParam(value = "bucket", defaultValue = "day") String bucket) {
    var res = (java.util.List<java.util.Map<String,Object>>) abnormalRate(type, from, to, bucket).data();
    StringBuilder sb = new StringBuilder();
    sb.append("bucket,total,abnormal,rate\n");
    for (var m : res) sb.append(m.get("bucket")).append(',').append(m.get("total")).append(',').append(m.get("abnormal")).append(',').append(m.get("rate")).append('\n');
    return org.springframework.http.ResponseEntity.ok().header("Content-Disposition","attachment; filename=abnormal-rate.csv").body(sb.toString());
  }

  private String formatBucket(LocalDate d, String bucket){
    return switch (bucket) {
      case "week" -> d.with(java.time.DayOfWeek.MONDAY).toString();
      case "month" -> d.withDayOfMonth(1).toString();
      default -> d.toString();
    };
  }
  private LocalDate nextBucket(LocalDate d, String bucket){
    return switch (bucket) {
      case "week" -> d.plusWeeks(1);
      case "month" -> d.plusMonths(1);
      default -> d.plusDays(1);
    };
  }

  private boolean isAbnormal(String type, String value){
    try {
      if (type.equalsIgnoreCase("BP")) {
        if (!value.contains("/")) return false; String[] p = value.split("/"); int sys = Integer.parseInt(p[0]); int dia = Integer.parseInt(p[1]);
        return sys>=140 || dia>=90 || sys<90 || dia<60;
      }
      if (type.equalsIgnoreCase("BG")) {
        double v = Double.parseDouble(value); return v>=7.0 || v<3.9;
      }
      if (type.equalsIgnoreCase("WEIGHT")) {
        double v = Double.parseDouble(value); return v<=0 || v>500;
      }
      return false;
    } catch (Exception e){ return false; }
  }
}
