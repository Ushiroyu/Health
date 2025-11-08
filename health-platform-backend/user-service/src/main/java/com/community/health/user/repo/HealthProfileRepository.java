package com.community.health.user.repo;

import com.community.health.user.entity.HealthProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long>, JpaSpecificationExecutor<HealthProfile> {
  Optional<HealthProfile> findByUserId(Long userId);

  Page<HealthProfile> findByChronicConditionsContainingIgnoreCase(String chronicConditions, Pageable pageable);
}

