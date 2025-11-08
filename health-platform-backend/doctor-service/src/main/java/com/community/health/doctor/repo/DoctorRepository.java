package com.community.health.doctor.repo;

import com.community.health.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
  List<Doctor> findByDeptId(Integer deptId);
  @Query("select d from Doctor d where concat(d.specialty,' ',d.title) like ?1")
  List<Doctor> searchByNameOrSpecialty(String like);
}
