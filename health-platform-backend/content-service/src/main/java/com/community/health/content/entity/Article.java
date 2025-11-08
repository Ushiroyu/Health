package com.community.health.content.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="article")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Article {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long articleId;
  private String title;
  @Lob
  private String content;
  private String category;
  private Long authorId;
  private LocalDateTime publishDate;
  @Column(length = 20)
  private String status; // DRAFT/PENDING/APPROVED/REJECTED
  private Long reviewerId;
  private LocalDateTime reviewedAt;
  @Lob
  private String rejectReason;
}
