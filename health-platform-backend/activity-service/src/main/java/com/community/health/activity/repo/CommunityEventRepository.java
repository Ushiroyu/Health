package com.community.health.activity.repo;

import com.community.health.activity.entity.CommunityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityEventRepository extends JpaRepository<CommunityEvent, Long>, JpaSpecificationExecutor<CommunityEvent> {
  Page<CommunityEvent> findByTitleContainingIgnoreCase(String q, Pageable p);
}
