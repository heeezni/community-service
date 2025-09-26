package com.example.communityservice.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.communityservice.dto.auth.AnonymousAuthRequestDTO;
import com.example.communityservice.dto.auth.UserInfoResponseDTO;
import com.example.communityservice.dto.posts.PostAttachmentsResponseDTO;
import com.example.communityservice.dto.posts.PostsRequestDTO;
import com.example.communityservice.dto.posts.PostsResponseDTO;
import com.example.communityservice.dto.posts.PostsSummaryDTO;
import com.example.communityservice.global.common.ApiResponseDTO;
import com.example.communityservice.global.common.PageResponseDTO;
import com.example.communityservice.service.AuthService;
import com.example.communityservice.service.FileUploadService;
import com.example.communityservice.service.PostsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 게시글 관련 REST API 컨트롤러 */
@Tag(name = "Posts", description = "게시글 관리 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostsController {

  private final PostsService postsService;
  private final FileUploadService fileUploadService;
  private final AuthService authService;

  // 게시글 목록 조회
  // GET /api/posts?category=전체&page=0&size=20&sort=views 또는 sort=createdAt
  @Operation(summary = "게시글 목록 조회")
  @GetMapping
  public ResponseEntity<ApiResponseDTO<PageResponseDTO<PostsSummaryDTO>>> getPosts(
      // @RequestParam : HTTP 요청의 URL 쿼리 파라미터를 메서드 파라미터로 가져오기
      @RequestParam(required = false) String category,
      @RequestParam(required = false, defaultValue = "createdAt") String sort,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) String search,
      Pageable pageable) {

    Page<PostsSummaryDTO> posts;

    // 태그 검색
    if (tag != null && !tag.trim().isEmpty()) {
      posts = postsService.getPostsByTag(tag, pageable);
    }
    // 키워드 검색
    else if (search != null && !search.trim().isEmpty()) {
      posts = postsService.searchPosts(search, pageable);
    }
    // 일반 목록 조회
    else if ("views".equals(sort)) {
      posts = postsService.getPopularPosts(category, pageable);
    } else if ("likes".equals(sort)) {
      posts = postsService.getPopularPostsByLikes(category, pageable);
    } else {
      posts = postsService.getPosts(category, pageable);
    }

    PageResponseDTO<PostsSummaryDTO> pageResponse = PageResponseDTO.from(posts);
    return ResponseEntity.ok(ApiResponseDTO.success(pageResponse));
  }

  // 게시글 상세 조회
  // GET /api/posts/{id}
  @Operation(summary = "게시글 상세 조회")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponseDTO<PostsResponseDTO>> getPost(
      @PathVariable Long id,
      // @RequestParam인데 URL에 없으면 defaultValue = "true"
      @RequestParam(defaultValue = "true") boolean incrementView,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

    // 로그인 여부와 관계없이 게시글 조회 자체는 가능
    Long userId = null;
    // 로그인한 경우 사용자 ID 추출
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      try {
        UserInfoResponseDTO userInfo = authService.validateTokenAndGetUser(authorizationHeader);
        userId = userInfo.getId();
      } catch (Exception e) {
        // 토큰이 유효하지 않아도 게시글 조회는 가능하므로 로그만 남기고 진행
        log.warn("토큰 유효성 검사 실패, 비로그인으로 처리: {}", e.getMessage());
      }
    }
    PostsResponseDTO post;
    if (incrementView) {
      post = postsService.getPost(id, userId); // 조회수 증가
    } else {
      post =
          postsService.getPostInfo(
              id, userId); // 조회수 증가 없음 (GET /api/posts/123?incrementView=false)
    }

    return ResponseEntity.ok(ApiResponseDTO.success(post));
  }

  // 게시글 작성
  // POST /api/posts
  @Operation(summary = "게시글 작성")
  @PostMapping
  public ResponseEntity<ApiResponseDTO<PostsResponseDTO>> createPost(
      @Valid @RequestBody PostsRequestDTO requestDto,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

    // Authorization 헤더가 있으면 무조건 인증된 사용자로 처리 (강제)
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      log.info("인증 헤더 감지, 토큰 유효성 검사 중...");
      try {
        UserInfoResponseDTO userInfo = authService.validateTokenAndGetUser(authorizationHeader);
        log.info(
            "사용자 정보 조회됨: id={}, username={}, email={}",
            userInfo.getId(),
            userInfo.getUsername(),
            userInfo.getEmail());
        // 로그인한 사용자는 무조건 해당 계정으로만 작성 가능
        requestDto.setAuthorId(userInfo.getId());
        requestDto.setIsAnonymous(false);
        requestDto.setAuthorName(userInfo.getUsername()); // 사용자 이름 설정
        // 익명 관련 필드 무시 (Defense in Depth)
        requestDto.setAnonymousEmail(null);
        requestDto.setAnonymousPassword(null);
        log.info(
            "요청 DTO 업데이트됨: authorId={}, isAnonymous={}",
            requestDto.getAuthorId(),
            requestDto.getIsAnonymous());
      } catch (Exception e) {
        log.error("토큰 유효성 검사 및 사용자 정보 조회 실패", e);
        throw e;
      }
    } else {
      log.info("유효한 인증 헤더 없음 (헤더: {}), 익명으로 진행", authorizationHeader);
    }

    log.info(
        "최종 요청 DTO: authorId={}, isAnonymous={}",
        requestDto.getAuthorId(),
        requestDto.getIsAnonymous());

    PostsResponseDTO createdPost = postsService.createPost(requestDto);
    return ResponseEntity.ok(ApiResponseDTO.success("게시글이 성공적으로 작성되었습니다.", createdPost));
  }

  // 게시글 수정
  // PUT /api/posts/{id}
  @Operation(summary = "게시글 수정")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponseDTO<PostsResponseDTO>> updatePost(
      @PathVariable Long id, @Valid @RequestBody PostsRequestDTO requestDto) {
    PostsResponseDTO updatedPost = postsService.updatePost(id, requestDto);
    return ResponseEntity.ok(ApiResponseDTO.success("게시글이 성공적으로 수정되었습니다.", updatedPost));
  }

  // 게시글 삭제
  // DELETE /api/posts/{id}
  @Operation(summary = "게시글 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponseDTO<Void>> deletePost(
      @PathVariable Long id, @RequestBody PostsRequestDTO requestDto) {
    postsService.deletePost(id, requestDto);
    return ResponseEntity.ok(ApiResponseDTO.success("게시글이 성공적으로 삭제되었습니다.", null));
  }

  // 좋아요 추가 (로그인 필수)
  // POST /api/posts/{id}/likes
  @Operation(summary = "좋아요 추가")
  @PostMapping("/{id}/likes")
  public ResponseEntity<ApiResponseDTO<Void>> addLike(
      @PathVariable Long id,
      @RequestHeader(value = "Authorization", required = true) String authorizationHeader) {

    // 로그인 체크
    UserInfoResponseDTO userInfo = authService.validateTokenAndGetUser(authorizationHeader);

    postsService.addLike(id, userInfo.getId());
    return ResponseEntity.ok(ApiResponseDTO.success("좋아요가 추가되었습니다.", null));
  }

  // 좋아요 취소 (로그인 필수)
  // DELETE /api/posts/{id}/likes
  @Operation(summary = "좋아요 취소")
  @DeleteMapping("/{id}/likes")
  public ResponseEntity<ApiResponseDTO<Void>> removeLike(
      @PathVariable Long id,
      @RequestHeader(value = "Authorization", required = true) String authorizationHeader) {

    // 로그인 체크
    UserInfoResponseDTO userInfo = authService.validateTokenAndGetUser(authorizationHeader);

    postsService.removeLike(id, userInfo.getId());
    return ResponseEntity.ok(ApiResponseDTO.success("좋아요가 취소되었습니다.", null));
  }

  // 익명 게시글 인증 확인
  // POST /api/posts/{id}/verify
  @Operation(summary = "익명 게시글 인증 확인")
  @PostMapping("/{id}/verify")
  public ResponseEntity<ApiResponseDTO<Void>> verifyAnonymousPost(
      @PathVariable Long id, @Valid @RequestBody AnonymousAuthRequestDTO requestDto) {
    postsService.verifyAnonymousPost(id, requestDto);
    return ResponseEntity.ok(ApiResponseDTO.success("인증이 성공했습니다.", null));
  }

  // === 첨부파일 관련 API ===

  // 첨부파일 목록 조회
  // GET /api/posts/{id}/attachments
  @Operation(summary = "첨부파일 목록 조회")
  @GetMapping("/{id}/attachments")
  public ResponseEntity<ApiResponseDTO<List<PostAttachmentsResponseDTO>>> getAttachments(
      @PathVariable Long id) {
    List<PostAttachmentsResponseDTO> attachments = fileUploadService.getAttachmentsByPostId(id);
    return ResponseEntity.ok(ApiResponseDTO.success(attachments));
  }

  // 첨부파일 업로드
  // POST /api/posts/{id}/attachments
  @Operation(summary = "첨부파일 업로드")
  @PostMapping("/{id}/attachments")
  public ResponseEntity<ApiResponseDTO<List<PostAttachmentsResponseDTO>>> uploadFiles(
      @PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
    List<PostAttachmentsResponseDTO> attachments =
        fileUploadService.uploadFiles(files, postsService.getPostEntity(id));
    return ResponseEntity.ok(ApiResponseDTO.success("파일이 성공적으로 업로드되었습니다.", attachments));
  }

  // 첨부파일 삭제
  // DELETE /api/posts/attachments/{attachmentId}
  @Operation(summary = "첨부파일 삭제")
  @DeleteMapping("/attachments/{attachmentId}")
  public ResponseEntity<ApiResponseDTO<Void>> deleteAttachment(@PathVariable Long attachmentId) {
    fileUploadService.deleteAttachment(attachmentId);
    return ResponseEntity.ok(ApiResponseDTO.success("첨부파일이 삭제되었습니다.", null));
  }

  // === 태그 관련 API ===

  // 모든 태그 목록 조회
  // GET /api/posts/tags
  @Operation(summary = "모든 태그 목록 조회")
  @GetMapping("/tags")
  public ResponseEntity<ApiResponseDTO<List<String>>> getAllTags() {
    List<String> allTags = postsService.getAllTags();
    return ResponseEntity.ok(ApiResponseDTO.success(allTags));
  }

  // === 사용자 좋아요 관련 API ===

  // 사용자가 좋아요한 게시글 목록 조회
  // GET /api/posts/users/{userId}/liked
  @Operation(summary = "사용자 좋아요 게시글 목록 조회")
  @GetMapping("/users/{userId}/liked")
  public ResponseEntity<ApiResponseDTO<PageResponseDTO<PostsSummaryDTO>>> getUserLikedPosts(
      @PathVariable Long userId,
      @RequestHeader(value = "Authorization", required = true) String authorizationHeader,
      Pageable pageable) {

    // 사용자 인증 및 권한 확인 (자신의 좋아요 목록만 조회 가능)
    UserInfoResponseDTO userInfo = authService.validateTokenAndGetUser(authorizationHeader);
    if (!userInfo.getId().equals(userId)) {
      throw new IllegalArgumentException("자신의 좋아요 목록만 조회할 수 있습니다.");
    }

    Page<PostsSummaryDTO> likedPosts = postsService.getLikedPostsByUser(userId, pageable);
    PageResponseDTO<PostsSummaryDTO> response =
        PageResponseDTO.from(likedPosts); // from() : 엔터티를 DTO로 반환

    return ResponseEntity.ok(ApiResponseDTO.success(response));
  }

  // 사용자가 작성한 게시글 목록 조회
  // GET /api/posts/users/{userId}/posts
  @Operation(summary = "사용자 작성 게시글 목록 조회")
  @GetMapping("/users/{userId}/posts")
  public ResponseEntity<ApiResponseDTO<PageResponseDTO<PostsSummaryDTO>>> getUserPosts(
      @PathVariable Long userId,
      @RequestHeader(value = "Authorization", required = true) String authorizationHeader,
      Pageable pageable) {

    // 사용자 인증 및 권한 확인 (자신의 작성글 목록만 조회 가능)
    UserInfoResponseDTO userInfo = authService.validateTokenAndGetUser(authorizationHeader);
    if (!userInfo.getId().equals(userId)) {
      throw new IllegalArgumentException("자신의 작성글 목록만 조회할 수 있습니다.");
    }

    Page<PostsSummaryDTO> userPosts = postsService.getPostsByUser(userId, pageable);
    PageResponseDTO<PostsSummaryDTO> response = PageResponseDTO.from(userPosts);

    return ResponseEntity.ok(ApiResponseDTO.success(response));
  }
}
