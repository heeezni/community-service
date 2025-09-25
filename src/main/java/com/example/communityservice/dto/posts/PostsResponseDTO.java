package com.example.communityservice.dto.posts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.communityservice.dto.comments.CommentsResponseDTO;
import com.example.communityservice.entity.PostCategory;
import com.example.communityservice.entity.Posts;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 게시글 상세 조회용 응답 DTO <br>
 * - 게시글의 모든 정보를 포함하여 클라이언트에게 전송 <br>
 * - 상세 보기 페이지에서 사용
 */
@Schema(description = "게시글 상세 조회 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostsResponseDTO {

  @Schema(description = "게시글 ID", example = "1")
  private Long postId;

  @Schema(description = "게시글 카테고리", example = "FREE_BOARD")
  private PostCategory category;

  @Schema(description = "게시글 제목", example = "맛있는 와인 추천해주세요!")
  private String title;

  @Schema(description = "게시글 내용", example = "안녕하세요! 와인 입문자인데 추천해주실 만한 와인 있을까요?")
  private String content;

  @Schema(description = "작성자명", example = "술린이")
  private String authorName;

  @Schema(description = "작성자 ID (회원인 경우만)", example = "123")
  @JsonProperty("author_id")
  private Long authorId;

  @Schema(description = "익명 사용자 여부", example = "false")
  private Boolean isAnonymous;

  @Schema(description = "조회수", example = "150")
  private Integer views;

  @Schema(description = "좋아요 수", example = "17")
  private Integer likes;

  @Schema(description = "현재 사용자가 좋아요를 눌렀는지 여부", example = "true")
  private Boolean isLikedByCurrentUser; // isLikedByCurrentUser는 서비스 레이어에서 처리

  @Schema(description = "댓글 수", example = "8")
  private Integer commentsCount;

  @Schema(description = "게시글 태그", example = "#와인 #추천 #초보자")
  private String tags;

  @Schema(description = "작성일시", example = "2025-09-25T10:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시", example = "2025-09-25T11:00:00")
  private LocalDateTime updatedAt;

  @Schema(description = "댓글 목록")
  private List<CommentsResponseDTO> comments;

  @Schema(description = "첨부파일 목록")
  private List<PostAttachmentsResponseDTO> attachments;

  /**
   * Posts 엔티티를 PostsResponseDto로 변환하는 정적 팩토리 메서드
   *
   * @param post 변환할 Posts 엔티티
   * @return PostsResponseDto 객체
   */
  public static PostsResponseDTO from(Posts post) {
    return PostsResponseDTO.builder()
        .postId(post.getPostId())
        .category(post.getCategory())
        .title(post.getTitle())
        .content(post.getContent())
        .authorName(post.getAuthors().getDisplayAuthorName())
        .authorId(post.getAuthors().getUserId()) // 회원 사용자의 ID 추가
        .isAnonymous(post.getAuthors().getIsAnonymous())
        .views(post.getViews())
        .likes(post.getLikes())
        .commentsCount(post.getCommentsCount())
        .tags(post.getTags())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .attachments(
            post.getAttachments().stream() // stream() : 리스트를 반복 처리할 준비 (게시글에 첨부된 파일 목록 꺼내기)
                .map(
                    PostAttachmentsResponseDTO
                        ::from) // map() : 스트림 안의 각 원소를 다른 형태로 변환 (자료구조 Map 아님 주의! mapping의 map)
                .collect(Collectors.toList()))
        .build();
  }
}
