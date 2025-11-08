package com.community.health.appointment.repo;

import com.community.health.appointment.entity.NotifyInbox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotifyInboxRepository extends JpaRepository<NotifyInbox, Long> {
  Page<NotifyInbox> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable p);
}

