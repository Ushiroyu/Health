package com.community.health.appointment.controller;

import com.community.health.appointment.service.AppointmentService;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {
  private final AppointmentService service;
  public AppointmentController(AppointmentService s){ this.service = s; }

  public record BookReq(Long doctorId, String date, String time, String symptom){}

  @io.swagger.v3.oas.annotations.Operation(summary = "Book appointment")
  @PostMapping("/book")
  public ApiResponse<Map<String,Object>> book(@RequestHeader("X-User-Id") Long uid,
                                              @RequestBody BookReq req){
    return service.book(uid, req.doctorId(), LocalDate.parse(req.date()), req.time(), req.symptom());
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "Cancel appointment")
  @PostMapping("/cancel/{id}")
  public ApiResponse<?> cancel(@RequestHeader("X-User-Id") Long uid, @PathVariable("id") Long id){
    return service.cancel(uid, id);
  }

  @GetMapping("/my")
  public ApiResponse<Page<?>> my(@RequestHeader("X-User-Id") Long uid,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "20") int size,
                                 @RequestParam(value = "status", required = false) String status) {
    return ApiResponse.ok(service.my(uid, page, size, status));
  }

  @GetMapping("/slots")
  public ApiResponse<?> slots(@RequestParam("doctorId") Long doctorId,
                              @RequestParam("date") String date) {
    return ApiResponse.ok(service.querySlots(doctorId, LocalDate.parse(date)));
  }

  @GetMapping("/search")
  public ApiResponse<?> search(@RequestParam(value = "doctorId", required = false) Long doctorId,
                               @RequestParam(value = "dateFrom", required = false) String dateFrom,
                               @RequestParam(value = "dateTo", required = false) String dateTo,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "symptom", required = false) String symptom,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size) {
    return ApiResponse.ok(service.search(doctorId, dateFrom, dateTo, status, symptom, page, size));
  }
}
