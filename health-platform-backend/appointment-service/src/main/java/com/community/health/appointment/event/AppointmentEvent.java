package com.community.health.appointment.event;

import java.time.LocalDate;

public class AppointmentEvent {
  private String type; // BOOKED/CANCELED
  private Long apptId;
  private Long userId;
  private Long doctorId;
  private LocalDate date;
  private String time;
  private long ts;

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public Long getApptId() { return apptId; }
  public void setApptId(Long apptId) { this.apptId = apptId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getDoctorId() { return doctorId; }
  public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; }
  public String getTime() { return time; }
  public void setTime(String time) { this.time = time; }
  public long getTs() { return ts; }
  public void setTs(long ts) { this.ts = ts; }
}

