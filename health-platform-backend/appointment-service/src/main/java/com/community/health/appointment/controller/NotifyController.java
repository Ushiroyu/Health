package com.community.health.appointment.controller;

import com.community.health.appointment.entity.NotifyInbox;
import com.community.health.appointment.repo.NotifyInboxRepository;
import com.community.health.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointment/notify")
public class NotifyController {
  private final NotifyInboxRepository repo;
  public NotifyController(NotifyInboxRepository r){ this.repo = r; }

  @GetMapping("/list")
  public ApiResponse<Page<NotifyInbox>> list(@RequestHeader("X-User-Id") Long uid,
                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "20") int size){
    return ApiResponse.ok(repo.findByUserIdOrderByCreatedAtDesc(uid, PageRequest.of(page, size)));
  }
}
