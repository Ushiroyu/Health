package com.community.health.appointment.repo;

import com.community.health.appointment.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  List<UserAccount> findByRoleIgnoreCase(String role);
  List<UserAccount> findByRoleIgnoreCaseAndStatusNot(String role, String status);
}
