package com.community.health.activity.repo;

import com.community.health.activity.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
  long countByEventId(Long eventId);
  Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
  Page<EventRegistration> findByUserIdOrderByRegTimeDesc(Long userId, Pageable pageable);
}
