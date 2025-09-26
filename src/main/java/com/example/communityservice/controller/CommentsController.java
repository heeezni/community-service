package com.example.communityservice.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.communityservice.dto.auth.AnonymousAuthRequestDTO;
import com.example.communityservice.dto.comments.CommentsRequestDTO;
import com.example.communityservice.dto.comments.CommentsResponseDTO;
import com.example.communityservice.global.common.ApiResponseDTO;
import com.example.communityservice.service.CommentsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** 댓글 관련 REST API 컨트롤러 */
@Tag(name = "Comments", description = "댓글 관리 API")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentsController {

  private final CommentsService commentsService;

  // 댓글 작성
  // POST /api/comments
  @Operation(summary = "댓글 작성")
  @PostMapping
  public ResponseEntity<ApiResponseDTO<CommentsResponseDTO>> createComment(
      @Valid @RequestBody CommentsRequestDTO requestDto) {
    CommentsResponseDTO createdComment = commentsService.createComment(requestDto);
    return ResponseEntity.ok(ApiResponseDTO.success("댓글이 성공적으로 작성되었습니다.", createdComment));
  }

  // 댓글 수정
  // PUT /api/comments/{id}
  @Operation(summary = "댓글 수정")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponseDTO<CommentsResponseDTO>> updateComment(
      @PathVariable Long id, // URL 경로의 일부를 변수로 가져오는 어노테이션
      @Valid @RequestBody CommentsRequestDTO requestDto) {
    CommentsResponseDTO updatedComment = commentsService.updateComment(id, requestDto);
    return ResponseEntity.ok(ApiResponseDTO.success("댓글이 성공적으로 수정되었습니다.", updatedComment));
  }

  // 댓글 삭제
  // DELETE /api/comments/{id}
  @Operation(summary = "댓글 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponseDTO<Void>> deleteComment(
      @PathVariable Long id, @RequestBody CommentsRequestDTO requestDto) {
    commentsService.deleteComment(id, requestDto);
    return ResponseEntity.ok(ApiResponseDTO.success("댓글이 성공적으로 삭제되었습니다.", null));
  }

  // 익명 댓글 인증 확인
  // POST /api/comments/{id}/verify
  @Operation(summary = "익명 댓글 인증 확인")
  @PostMapping("/{id}/verify")
  public ResponseEntity<ApiResponseDTO<Void>> verifyAnonymousComment(
      @PathVariable Long id, @Valid @RequestBody AnonymousAuthRequestDTO requestDto) {
    commentsService.verifyAnonymousComment(id, requestDto);
    return ResponseEntity.ok(ApiResponseDTO.success("인증이 성공했습니다.", null));
  }

  // === User Service용 API ===

  // 특정 작성자의 댓글 목록 조회
  // GET /api/comments/author/{authorId}
  @Operation(summary = "작성자별 댓글 목록 조회")
  @GetMapping("/author/{authorId}")
  public ResponseEntity<ApiResponseDTO<List<CommentsResponseDTO>>> getCommentsByAuthor(
      @PathVariable Long authorId) {
    List<CommentsResponseDTO> comments = commentsService.getCommentsByAuthorId(authorId);
    return ResponseEntity.ok(ApiResponseDTO.success(comments));
  }
}
