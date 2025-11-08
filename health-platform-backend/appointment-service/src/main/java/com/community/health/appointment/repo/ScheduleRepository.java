package com.community.health.appointment.repo;

import com.community.health.appointment.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
  List<Schedule> findByDoctorIdAndDateOrderByTimeSlotAsc(Long doctorId, LocalDate date);
  Optional<Schedule> findByDoctorIdAndDateAndTimeSlot(Long doctorId, LocalDate date, String timeSlot);
  List<Schedule> findByDate(LocalDate date);
}
