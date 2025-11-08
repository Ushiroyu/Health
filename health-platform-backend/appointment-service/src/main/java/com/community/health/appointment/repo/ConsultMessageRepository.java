package com.community.health.appointment.repo;

import com.community.health.appointment.entity.ConsultMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultMessageRepository extends JpaRepository<ConsultMessage, Long> {
  Page<ConsultMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId, Pageable p);
}

