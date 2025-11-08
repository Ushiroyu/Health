package com.community.health.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="community_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityEvent {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long eventId;
  private String title;
  @Lob
  private String description;
  private LocalDateTime eventDate;
  private String location;
  private Integer capacity;
  private String organizer;
  @Column(length = 20)
  private String status; // DRAFT/PUBLISHED/CLOSED
}
