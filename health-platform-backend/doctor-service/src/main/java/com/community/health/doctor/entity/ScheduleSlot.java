package com.community.health.doctor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 只读视图，用于 doctor-service 查询预约排班。
 */
@Entity
@Table(name = "schedule")
@Getter
@Setter
public class ScheduleSlot {
  @Id
  @Column(name = "schedule_id")
  private Long scheduleId;

  @Column(name = "doctor_id")
  private Long doctorId;

  private LocalDate date;

  @Column(name = "time_slot")
  private String timeSlot;

  private Integer capacity;
  private String status;
}

