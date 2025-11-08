package com.community.health.doctor.repo;

import com.community.health.doctor.entity.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {
  List<ScheduleSlot> findByDoctorIdAndDate(Long doctorId, LocalDate date);
  List<ScheduleSlot> findByDoctorIdAndDateAndTimeSlot(Long doctorId, LocalDate date, String timeSlot);
  List<ScheduleSlot> findByDoctorIdInAndDate(java.util.Collection<Long> doctorIds, LocalDate date);
  List<ScheduleSlot> findByDoctorIdInAndDateAndTimeSlot(java.util.Collection<Long> doctorIds, LocalDate date, String timeSlot);
}
