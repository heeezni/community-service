package com.example.communityservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.communityservice.dto.auth.AnonymousAuthRequestDTO;
import com.example.communityservice.dto.comments.CommentsRequestDTO;
import com.example.communityservice.dto.comments.CommentsResponseDTO;
import com.example.communityservice.entity.Authors;
import com.example.communityservice.entity.Comments;
import com.example.communityservice.entity.Posts;
import com.example.communityservice.global.exception.BusinessException;
import com.example.communityservice.global.exception.ErrorCode;
import com.example.communityservice.repository.AuthorsRepository;
import com.example.communityservice.repository.CommentsRepository;
import com.example.communityservice.repository.PostsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 댓글 관련 비즈니스 로직 처리 서비스 <br>
 * - 게시글별 댓글 생성/수정/삭제 <br>
 * - 회원/익명 사용자별 댓글 권한 관리 <br>
 * - 익명 사용자 인증 처리 <br>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 모든 메서드는 조회용 트랜잭션이 됨
public class CommentsService {

  private final CommentsRepository commentsRepository;
  private final PostsRepository postsRepository;
  private final AuthorsRepository authorsRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 특정 작성자의 댓글 목록 조회 (User MyPage용 내부 API)
   *
   * @param authorId 작성자 ID
   * @return 댓글 목록 (최신순)
   */
  public List<CommentsResponseDTO> getCommentsByAuthorId(Long authorId) {
    List<Comments> comments =
        commentsRepository.findByAuthors_AuthorIdOrderByCreatedAtDesc(authorId);
    return comments.stream().map(CommentsResponseDTO::from).collect(Collectors.toList());
  }

  /**
   * 댓글 생성
   *
   * @param requestDto 댓글 작성 요청 정보
   * @return 생성된 댓글 정보
   */
  @Transactional // 쓰기용만 별도로 @Transactional 붙이면 클래스 레벨설정 오버라이드
  public CommentsResponseDTO createComment(CommentsRequestDTO requestDto) {
    // 게시글 존재 확인
    Posts post =
        postsRepository
            .findById(requestDto.getPostId())
            .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

    // 작성자 정보 조회 또는 생성
    Authors author = getOrCreateAuthor(requestDto);

    Comments comment =
        Comments.builder().post(post).authors(author).content(requestDto.getContent()).build();

    Comments savedComment = commentsRepository.save(comment);
    return CommentsResponseDTO.from(savedComment);
  }

  /**
   * 댓글 수정
   *
   * @param commentId 댓글 ID
   * @param requestDto 댓글 수정 요청 정보
   * @return 수정된 댓글 정보
   */
  @Transactional
  public CommentsResponseDTO updateComment(Long commentId, CommentsRequestDTO requestDto) {
    Comments comment =
        commentsRepository
            .findById(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

    // 작성자 권한 체크
    validateAuthorPermission(comment, requestDto);

    comment.updateContent(requestDto.getContent());
    return CommentsResponseDTO.from(comment);
  }

  /**
   * 댓글 삭제
   *
   * @param commentId 댓글 ID
   * @param requestDto 작성자 권한 확인용 정보
   */
  @Transactional
  public void deleteComment(Long commentId, CommentsRequestDTO requestDto) {
    Comments comment =
        commentsRepository
            .findById(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

    // 작성자 권한 체크
    validateAuthorPermission(comment, requestDto);

    commentsRepository.delete(comment);
  }

  /**
   * 작성자 정보 조회 또는 생성 (회원/익명 구분)
   *
   * @param requestDto 댓글 작성 요청 정보
   * @return Authors 엔티티
   */
  private Authors getOrCreateAuthor(CommentsRequestDTO requestDto) {
    // 로그인한 사용자가 익명으로 작성하려 하는 경우 차단
    if (requestDto.getAuthorId() != null && Boolean.TRUE.equals(requestDto.getIsAnonymous())) {
      throw new IllegalArgumentException("로그인한 사용자는 익명으로 작성할 수 없습니다.");
    }

    if (Boolean.TRUE.equals(requestDto.getIsAnonymous())) {
      // 익명 사용자 처리
      String encodedPassword = passwordEncoder.encode(requestDto.getAnonymousPassword());
      return authorsRepository.save(
          Authors.createAnonymousAuthor(requestDto.getAnonymousEmail(), encodedPassword));
    } else {
      // 회원 사용자 처리
      if (requestDto.getAuthorId() == null) {
        throw new IllegalArgumentException("회원 작성자 ID는 필수입니다.");
      }
      return authorsRepository
          .findByUserId(requestDto.getAuthorId())
          .orElseGet(
              () -> {
                Authors newAuthor =
                    Authors.createMemberAuthor(
                        requestDto.getAuthorId(), requestDto.getAuthorName());
                return authorsRepository.save(newAuthor);
              });
    }
  }

  /**
   * 익명 댓글 인증 확인 (수정/삭제 전용)
   *
   * @param commentId 댓글 ID
   * @param requestDto 익명 인증 요청 정보
   */
  public void verifyAnonymousComment(Long commentId, AnonymousAuthRequestDTO requestDto) {
    Comments comment =
        commentsRepository
            .findById(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

    Authors commentAuthor = comment.getAuthors();

    if (!commentAuthor.getIsAnonymous()) {
      throw new IllegalArgumentException("익명 댓글이 아닙니다.");
    }

    // 익명 사용자 검증: 이메일과 비밀번호 확인
    if (!commentAuthor.getAnonymousEmail().equals(requestDto.getAnonymousEmail())
        || !passwordEncoder.matches(
            requestDto.getAnonymousPassword(), commentAuthor.getAnonymousPwd())) {
      throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
    }
  }

  /**
   * 댓글작성자 권한 검증 (회원/익명 구분)
   *
   * @param comment 댓글 엔티티
   * @param requestDto 권한 확인용 요청 정보
   */
  private void validateAuthorPermission(Comments comment, CommentsRequestDTO requestDto) {
    Authors commentAuthor = comment.getAuthors();

    if (commentAuthor.getIsAnonymous()) {
      // 익명 사용자 검증: 이메일과 비밀번호 확인
      if (!commentAuthor.getAnonymousEmail().equals(requestDto.getAnonymousEmail())
          || !passwordEncoder.matches(
              requestDto.getAnonymousPassword(), commentAuthor.getAnonymousPwd())) {
        throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
      }
    } else {
      // 회원 사용자 검증: 사용자 ID 확인 (userId 기준)
      if (!commentAuthor.getUserId().equals(requestDto.getAuthorId())) {
        throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
      }
    }
  }
}
