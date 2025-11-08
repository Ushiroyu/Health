package com.community.health.appointment.repo;

import com.community.health.appointment.entity.ConsultSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultSessionRepository extends JpaRepository<ConsultSession, Long> {
  Page<ConsultSession> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable p);
  Page<ConsultSession> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable p);
}

