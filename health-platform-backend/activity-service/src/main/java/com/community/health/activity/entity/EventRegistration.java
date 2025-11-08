package com.community.health.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="event_registration", uniqueConstraints = @UniqueConstraint(name="uniq_event_user", columnNames={"eventId","userId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventRegistration {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long regId;
  private Long eventId;
  private Long userId;
  private LocalDateTime regTime;
}
