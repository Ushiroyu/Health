package com.community.health.appointment.repo;

import com.community.health.appointment.entity.GuidancePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuidancePlanRepository extends JpaRepository<GuidancePlan, Long> {
  Page<GuidancePlan> findByUserIdOrderByStartAtDesc(Long userId, Pageable p);
}

