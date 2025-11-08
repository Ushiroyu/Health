package com.community.health.user.repo;

import com.community.health.user.entity.HealthRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
  Page<HealthRecord> findByUserIdAndTypeOrderByRecordDateDesc(Long userId, String type, Pageable p);

  interface DailyAgg {
    java.time.LocalDate getD();
    Long getTotal();
  }

  @Query("select hr.recordDate as d, count(hr) as total from HealthRecord hr where hr.type = :type and hr.recordDate between :from and :to group by hr.recordDate order by hr.recordDate")
  java.util.List<DailyAgg> aggregateByDate(@Param("type") String type, @Param("from") java.time.LocalDate from, @Param("to") java.time.LocalDate to);

  java.util.List<HealthRecord> findByTypeAndRecordDateBetween(String type, java.time.LocalDate from, java.time.LocalDate to);
}
