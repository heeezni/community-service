package com.example.communityservice.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

@Entity
@Table(
    name = "posts",
    indexes = {
      @Index(name = "idx_author_id", columnList = "author_id"), // 작성자별 게시글 조회
      @Index(name = "idx_category", columnList = "category"), // 카테고리별 게시글 목록
      @Index(name = "idx_created_at", columnList = "created_at") // 최신순 정렬
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Posts {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id")
  private Long postId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private Authors authors;

  @Column(name = "category", nullable = false, length = 50)
  @Enumerated(EnumType.STRING) // 데이터베이스에 문자열로 저장 (기본값인 ORDINAL보다 안전)
  private PostCategory category;

  @Column(name = "title", nullable = false, length = 500)
  private String title;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "views", nullable = false)
  @Builder.Default
  private Integer views = 0;

  @Column(name = "likes", nullable = false)
  @Builder.Default
  private Integer likes = 0;

  @Column(name = "tags", columnDefinition = "JSON")
  private String tags;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // 연관관계 매핑
  @OneToMany(
      mappedBy = "post",
      cascade = CascadeType.ALL,
      orphanRemoval = true) // OneToMany는 기본이 LAZY
  @Builder.Default
  private List<Comments> comments = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PostAttachments> attachments = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PostLikes> postLikes = new ArrayList<>();

  // 비즈니스 메서드

  /** 게시글 조회수 증가 게시글 상세 조회 시 호출하여 조회수를 1 증가시킴 */
  public void increaseViews() {
    this.views++;
  }

  /**
   * 게시글 정보 수정 게시글 수정 시 제목, 내용, 카테고리, 태그를 일괄 업데이트
   *
   * @param title 수정할 제목
   * @param content 수정할 내용
   * @param category 수정할 카테고리
   * @param tags 수정할 태그 (JSON 형태)
   */
  public void updatePost(String title, String content, PostCategory category, String tags) {
    this.title = title;
    this.content = content;
    this.category = category;
    this.tags = tags;
  }

  /**
   * 댓글 개수 조회 연관된 댓글 컬렉션의 크기를 반환하여 댓글 개수를 제공
   *
   * @return 댓글 개수
   */
  public int getCommentsCount() {
    return comments.size();
  }
}
