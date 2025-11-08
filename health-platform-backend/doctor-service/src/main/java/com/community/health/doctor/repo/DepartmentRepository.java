package com.community.health.doctor.repo;

import com.community.health.doctor.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {}

