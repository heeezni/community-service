package com.example.communityservice.dto.posts;

import java.time.LocalDateTime;

import com.example.communityservice.entity.PostCategory;
import com.example.communityservice.entity.Posts;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 게시글 목록 조회용 요약 DTO <br>
 * - 게시글 목록에서 보여줄 기본 정보만 포함 <br>
 * - content(내용)는 제외하여 데이터 전송량 최적화
 */
@Schema(description = "게시글 목록 조회 요약 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostsSummaryDTO {

  @Schema(description = "게시글 ID", example = "1")
  private Long postId;

  @Schema(description = "게시글 카테고리", example = "FREE_BOARD")
  private PostCategory category;

  @Schema(description = "게시글 제목", example = "맛있는 와인 추천해주세요!")
  private String title;

  @Schema(description = "작성자명", example = "술린이")
  private String authorName;

  @Schema(description = "익명 사용자 여부", example = "false")
  private Boolean isAnonymous;

  @Schema(description = "조회수", example = "150")
  private Integer views;

  @Schema(description = "좋아요 수", example = "17")
  private Integer likes;

  @Schema(description = "댓글 수", example = "8")
  private Integer commentsCount;

  @Schema(description = "작성일시", example = "2025-09-25T10:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "첨부파일 있는지 여부", example = "true")
  private Boolean hasAttachments;

  /**
   * Posts 엔티티를 PostsSummaryDto로 변환하는 정적 팩토리 메서드
   *
   * @param post 변환할 Posts 엔티티
   * @return PostsSummaryDto 객체
   */
  public static PostsSummaryDTO from(Posts post) {
    return PostsSummaryDTO.builder()
        .postId(post.getPostId())
        .category(post.getCategory())
        .title(post.getTitle())
        .authorName(post.getAuthors().getDisplayAuthorName())
        .isAnonymous(post.getAuthors().getIsAnonymous())
        .views(post.getViews())
        .likes(post.getLikes())
        .commentsCount(post.getCommentsCount())
        .createdAt(post.getCreatedAt())
        .hasAttachments(
            !post.getAttachments()
                .isEmpty()) // 게시글에 첨부파일이 있으면 DTO의 hasAttachments를 true로, 없으면 false로 세팅
        .build();
  }
}
