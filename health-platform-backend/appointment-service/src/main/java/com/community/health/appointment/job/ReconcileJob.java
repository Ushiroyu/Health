package com.community.health.appointment.job;

import com.community.health.appointment.repo.AppointmentRepository;
import com.community.health.appointment.repo.ScheduleRepository;
import com.community.health.appointment.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReconcileJob {
  private static final Logger log = LoggerFactory.getLogger(ReconcileJob.class);
  private final ScheduleRepository schedules;
  private final AppointmentRepository appts;
  private final AppointmentService service;
  public ReconcileJob(ScheduleRepository s, AppointmentRepository a, AppointmentService svc){ this.schedules = s; this.appts = a; this.service = svc; }

  // 每5分钟校准今天和明天的剩余数
  @Scheduled(cron = "0 */5 * * * *")
  public void reconcileTodayTomorrow(){
    reconcileFor(LocalDate.now());
    reconcileFor(LocalDate.now().plusDays(1));
  }

  private void reconcileFor(LocalDate date){
    var list = schedules.findByDate(date);
    for (var s : list) {
      int capacity = s.getCapacity()==null?0:s.getCapacity();
      long booked = appts.countByDoctorIdAndApptDateAndApptTimeAndStatus(s.getDoctorId(), s.getDate(), s.getTimeSlot(), "booked");
      int left = Math.max(0, capacity - (int)booked);
      service.setSlot(s.getDoctorId(), s.getDate(), s.getTimeSlot(), left);
      log.debug("reconcile slot doctor={} date={} time={} left={}", s.getDoctorId(), s.getDate(), s.getTimeSlot(), left);
    }
  }
}
