package com.community.health.appointment.controller;

import com.community.health.appointment.entity.Appointment;
import com.community.health.appointment.repo.AppointmentRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping({"/admin/stats", "/appointment/admin/stats"})
public class AdminStatsController {
  private final AppointmentRepository repo;
  public AdminStatsController(AppointmentRepository r){ this.repo = r; }

  @GetMapping("/appointments")
  public ApiResponse<?> appt(@RequestParam("from") String from,
                             @RequestParam("to") String to,
                             @RequestParam(value = "bucket", defaultValue = "day") String bucket) {
    LocalDate f = LocalDate.parse(from), t = LocalDate.parse(to);
    Map<String, long[]> buckets = new LinkedHashMap<>(); // total, canceled
    LocalDate cur = f; while (!cur.isAfter(t)) { buckets.put(format(cur, bucket), new long[]{0,0}); cur = next(cur, bucket); }
    var daily = repo.aggregateByDate(f, t);
    for (var d : daily) {
      String key = format(d.getD(), bucket);
      var arr = buckets.get(key); if (arr==null) continue; arr[0]+= d.getTotal()==null?0:d.getTotal(); arr[1]+= d.getCanceled()==null?0:d.getCanceled();
    }
    List<Map<String,Object>> out = new ArrayList<>();
    for (var e : buckets.entrySet()) {
      long total = e.getValue()[0], canceled = e.getValue()[1];
      out.add(Map.of("bucket", e.getKey(), "total", total, "canceled", canceled));
    }
    return ApiResponse.ok(out);
  }

  @GetMapping(value = "/appointments/export", produces = "text/csv;charset=UTF-8")
  public org.springframework.http.ResponseEntity<String> exportAppt(@RequestParam("from") String from,
                                                                    @RequestParam("to") String to,
                                                                    @RequestParam(value = "bucket", defaultValue = "day") String bucket) {
    var res = (java.util.List<java.util.Map<String,Object>>) appt(from, to, bucket).data();
    StringBuilder sb = new StringBuilder();
    sb.append("bucket,total,canceled\n");
    for (var m : res) {
      sb.append(m.get("bucket")).append(',').append(m.get("total")).append(',').append(m.get("canceled")).append('\n');
    }
    return org.springframework.http.ResponseEntity.ok()
      .header("Content-Disposition", "attachment; filename=appointments-agg.csv")
      .body(sb.toString());
  }

  private String format(LocalDate d, String bucket){
    return switch (bucket) {
      case "week" -> d.with(java.time.DayOfWeek.MONDAY).toString();
      case "month" -> d.withDayOfMonth(1).toString();
      default -> d.toString();
    };
  }
  private LocalDate next(LocalDate d, String bucket){
    return switch (bucket) {
      case "week" -> d.plusWeeks(1);
      case "month" -> d.plusMonths(1);
      default -> d.plusDays(1);
    };
  }
}
