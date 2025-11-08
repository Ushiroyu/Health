package com.community.health.appointment.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "schedule", uniqueConstraints = @UniqueConstraint(name = "uniq_doc_date_slot", columnNames = {"doctorId","date","timeSlot"}))
public class Schedule {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long scheduleId;
  private Long doctorId;
  private LocalDate date;
  private String timeSlot; // e.g. 10:00-10:30
  private Integer capacity; // total capacity for the slot
  private String status; // OPEN/CLOSED

  public Long getScheduleId() { return scheduleId; }
  public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
  public Long getDoctorId() { return doctorId; }
  public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; }
  public String getTimeSlot() { return timeSlot; }
  public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
  public Integer getCapacity() { return capacity; }
  public void setCapacity(Integer capacity) { this.capacity = capacity; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}

