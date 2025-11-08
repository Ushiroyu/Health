package com.community.health.appointment.repo;

import com.community.health.appointment.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
  Page<Prescription> findByUserIdOrderByPrescDateDesc(Long userId, Pageable p);
  Page<Prescription> findByDoctorIdOrderByPrescDateDesc(Long doctorId, Pageable p);
}

