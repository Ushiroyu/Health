package com.community.health.appointment.repo;

import com.community.health.appointment.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
  Optional<Appointment> findByDoctorIdAndApptDateAndApptTime(Long doctorId, LocalDate date, String time);
  Page<Appointment> findByUserIdOrderByApptDateDesc(Long userId, Pageable p);
  Page<Appointment> findByUserIdAndStatusOrderByApptDateDesc(Long userId, String status, Pageable p);
  long countByDoctorIdAndApptDateAndApptTimeAndStatus(Long doctorId, LocalDate apptDate, String apptTime, String status);

  interface ApptDailyAgg {
    LocalDate getD();
    Long getTotal();
    Long getCanceled();
  }

  @Query("select a.apptDate as d, count(a) as total, sum(case when a.status='canceled' then 1 else 0 end) as canceled " +
         "from Appointment a where a.apptDate between :from and :to group by a.apptDate order by a.apptDate")
  List<ApptDailyAgg> aggregateByDate(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
