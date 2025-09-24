package com.example.communityservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

@Entity
@Table(
    name = "comments",
    indexes = {
      @Index(name = "idx_post_id", columnList = "post_id") // 특정 게시글의 댓글 조회
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comments {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_id")
  private Long commentId;

  @ManyToOne(fetch = FetchType.LAZY) // 지연로딩 : 필요한 순간에 추가 쿼리를 실행해서 데이터 가져오기
  @JoinColumn(name = "post_id", nullable = false)
  private Posts post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private Authors authors;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // 비즈니스 메서드

  /**
   * 댓글 내용 수정 댓글 내용을 검증하고 업데이트하는 비즈니스 로직 빈 문자열과 길이 제한을 검증하여 데이터 품질 보장
   *
   * @param content 수정할 댓글 내용
   * @throws IllegalArgumentException 내용이 비어있거나 1000자 초과 시
   */
  public void updateContent(String content) {
    if (content.trim().isEmpty()) {
      throw new IllegalArgumentException("댓글 내용은 비어있을 수 없습니다");
    }
    if (content.trim().length() > 1000) {
      throw new IllegalArgumentException("댓글은 1000자를 초과할 수 없습니다");
    }
    this.content = content.trim();
  }

  // 작성자 정보 조회 편의 메서드
  /**
   * 작성자 이름 조회 Authors 엔티티의 이름을 위임하여 반환
   *
   * @return 작성자 이름
   */
  public String getAuthorName() {
    return authors.getAuthorName();
  }

  /**
   * 익명 작성자 여부 확인 Authors 엔티티의 익명 여부를 위임하여 반환
   *
   * @return 익명 여부 (true: 익명, false: 회원)
   */
  public boolean isAnonymous() {
    return authors.getIsAnonymous();
  }
}
