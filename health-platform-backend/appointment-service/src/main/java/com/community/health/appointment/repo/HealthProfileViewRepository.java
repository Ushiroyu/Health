package com.community.health.appointment.repo;

import com.community.health.appointment.entity.HealthProfileView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthProfileViewRepository extends JpaRepository<HealthProfileView, Long> {
  List<HealthProfileView> findByChronicConditionsContainingIgnoreCase(String keyword);
}

