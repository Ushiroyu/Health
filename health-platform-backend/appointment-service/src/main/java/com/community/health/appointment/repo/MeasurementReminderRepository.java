package com.community.health.appointment.repo;

import com.community.health.appointment.entity.MeasurementReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MeasurementReminderRepository extends JpaRepository<MeasurementReminder, Long> {
  List<MeasurementReminder> findByEnabledTrueAndDailyTime(String dailyTime);
  List<MeasurementReminder> findByUserIdOrderByReminderIdDesc(Long userId);
}

