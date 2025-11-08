package com.community.health.doctor.repo;

import com.community.health.doctor.entity.DoctorSymptomTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorSymptomTagRepository extends JpaRepository<DoctorSymptomTag, Long> {
  List<DoctorSymptomTag> findByDoctorId(Long doctorId);
  List<DoctorSymptomTag> findByDoctorIdIn(List<Long> doctorIds);
  void deleteByDoctorId(Long doctorId);
}
