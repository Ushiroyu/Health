package com.community.health.appointment.controller;

import com.community.health.appointment.entity.Prescription;
import com.community.health.appointment.repo.PrescriptionRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/appointment/prescription")
public class PrescriptionController {
  private final PrescriptionRepository repo;
  private final com.community.health.appointment.repo.AuditLogRepository auditRepo;
  public PrescriptionController(PrescriptionRepository r, com.community.health.appointment.repo.AuditLogRepository a){ this.repo = r; this.auditRepo = a; }

  public record PrescReq(Long appointmentId, Long userId, Long doctorId, String medicines, String advice){}

  @PostMapping
  public ApiResponse<Map<String,Object>> create(@RequestHeader("X-User-Id") Long doctorUserId,
                                                @RequestHeader("X-User-Role") String role,
                                                @RequestBody PrescReq req){
    // 角色校验已经在拦截器做，这里仅保护性判断
    if (role==null || !(role.equalsIgnoreCase("DOCTOR") || role.equalsIgnoreCase("ADMIN"))) {
      return ApiResponse.error("Forbidden");
    }
    Prescription p = repo.save(Prescription.builder()
      .appointmentId(req.appointmentId()).userId(req.userId()).doctorId(req.doctorId())
      .prescDate(LocalDateTime.now()).medicines(req.medicines()).advice(req.advice()).build());
    // audit
    auditRepo.save(com.community.health.appointment.entity.AuditLog.builder().ts(LocalDateTime.now()).userId(doctorUserId).action("CREATE_PRESC").targetType("PRESC").targetId(String.valueOf(p.getPrescId())).details(null).build());
    return ApiResponse.ok(Map.of("prescId", p.getPrescId()));
  }

  @GetMapping("/{id}")
  public ApiResponse<?> get(@RequestHeader("X-User-Id") Long uid,
                            @RequestHeader(value = "X-User-Role", required = false) String role,
                            @PathVariable("id") Long id){
    return repo.findById(id).<ApiResponse<?>>map(p -> {
      if (!(p.getUserId().equals(uid) || (role!=null && (role.equalsIgnoreCase("DOCTOR")||role.equalsIgnoreCase("ADMIN")))))
        return ApiResponse.error("无权查看");
      return ApiResponse.ok(p);
    }).orElseGet(() -> ApiResponse.error("处方不存在"));
  }

  @GetMapping("/list")
  public ApiResponse<Page<Prescription>> list(@RequestParam(value = "userId", required = false) Long userId,
                                              @RequestParam(value = "doctorId", required = false) Long doctorId,
                                              @RequestParam(value = "page", defaultValue = "0") int page,
                                              @RequestParam(value = "size", defaultValue = "20") int size){
    var p = PageRequest.of(page, size);
    if (userId != null) return ApiResponse.ok(repo.findByUserIdOrderByPrescDateDesc(userId, p));
    if (doctorId != null) return ApiResponse.ok(repo.findByDoctorIdOrderByPrescDateDesc(doctorId, p));
    return ApiResponse.ok(Page.empty());
  }
}
