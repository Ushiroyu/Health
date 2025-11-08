package com.community.health.appointment.controller;

import com.community.health.appointment.entity.ConsultMessage;
import com.community.health.appointment.entity.ConsultSession;
import com.community.health.appointment.repo.ConsultMessageRepository;
import com.community.health.appointment.repo.ConsultSessionRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/appointment/consult")
public class ConsultController {
  private final ConsultSessionRepository sessionRepo;
  private final ConsultMessageRepository msgRepo;
  public ConsultController(ConsultSessionRepository s, ConsultMessageRepository m){ this.sessionRepo = s; this.msgRepo = m; }

  public record SessionReq(Long doctorId, String chiefComplaint){}
  public record MsgReq(Long sessionId, String contentType, String content){}

  @GetMapping("/session/list")
  public ApiResponse<Page<ConsultSession>> list(@RequestHeader("X-User-Id") Long uid,
                                                @RequestHeader(value = "X-User-Role", required = false) String role,
                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "20") int size,
                                                @RequestParam(value = "userId", required = false) Long userId,
                                                @RequestParam(value = "doctorId", required = false) Long doctorId){
    var pageable = PageRequest.of(page, size);
    if (doctorId != null) {
      if (role == null || !(role.equalsIgnoreCase("DOCTOR") || role.equalsIgnoreCase("ADMIN"))) {
        return ApiResponse.error("仅医生或管理员可按医生查看会话");
      }
      return ApiResponse.ok(sessionRepo.findByDoctorIdOrderByCreatedAtDesc(doctorId, pageable));
    }
    Long targetUserId = userId != null ? userId : uid;
    return ApiResponse.ok(sessionRepo.findByUserIdOrderByCreatedAtDesc(targetUserId, pageable));
  }

  @PostMapping("/session")
  public ApiResponse<Map<String,Object>> create(@RequestHeader("X-User-Id") Long uid,
                                                @RequestBody SessionReq req){
    ConsultSession cs = sessionRepo.save(ConsultSession.builder()
      .userId(uid).doctorId(req.doctorId()).status("OPEN").chiefComplaint(req.chiefComplaint())
      .createdAt(LocalDateTime.now()).build());
    return ApiResponse.ok(Map.of("sessionId", cs.getSessionId()));
  }

  @PostMapping("/session/{id}/accept")
  public ApiResponse<?> accept(@PathVariable("id") Long id){
    return sessionRepo.findById(id).map(s -> {
      s.setStatus("OPEN");
      sessionRepo.save(s);
      return ApiResponse.ok();
    }).orElseGet(() -> ApiResponse.error("会话不存在"));
  }

  @PostMapping("/session/{id}/close")
  public ApiResponse<?> close(@PathVariable("id") Long id){
    return sessionRepo.findById(id).map(s -> {
      s.setStatus("CLOSED"); s.setClosedAt(LocalDateTime.now());
      sessionRepo.save(s);
      return ApiResponse.ok();
    }).orElseGet(() -> ApiResponse.error("会话不存在"));
  }

  @PostMapping("/message")
  public ApiResponse<Map<String,Object>> send(@RequestHeader("X-User-Id") Long uid,
                                              @RequestHeader(value = "X-User-Role", required = false) String role,
                                              @RequestBody MsgReq req){
    var s = sessionRepo.findById(req.sessionId()).orElse(null);
    if (s == null) return ApiResponse.error("会话不存在");
    boolean isDoctor = role!=null && (role.equalsIgnoreCase("DOCTOR")||role.equalsIgnoreCase("ADMIN"));
    if (!(isDoctor || uid.equals(s.getUserId()))) return ApiResponse.error("无权发送");
    String sender = isDoctor?"DOCTOR":"USER";
    ConsultMessage msg = msgRepo.save(ConsultMessage.builder()
      .sessionId(req.sessionId()).senderType(sender).contentType(req.contentType()).content(req.content())
      .createdAt(LocalDateTime.now()).read(false).build());
    return ApiResponse.ok(Map.of("msgId", msg.getMsgId()));
  }

  @GetMapping("/messages")
  public ApiResponse<Page<ConsultMessage>> messages(@RequestParam("sessionId") Long sessionId,
                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "50") int size){
    return ApiResponse.ok(msgRepo.findBySessionIdOrderByCreatedAtAsc(sessionId, PageRequest.of(page, size)));
  }
}
