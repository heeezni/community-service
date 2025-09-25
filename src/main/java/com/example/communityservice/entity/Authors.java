package com.example.communityservice.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(
    name = "authors",
    indexes = {
      @Index(name = "idx_user_id", columnList = "user_id"), // 회원별 작성 내역 조회
      @Index(name = "idx_anonymous_email", columnList = "anonymous_email") // 익명 사용자 인증
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authors {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "author_id")
  private Long authorId;

  @Column(name = "user_id")
  private Long userId; // User Service 참조값 (회원일 때만)

  @Column(name = "author_name", length = 100)
  private String authorName; // 작성자명 (회원명 또는 익명닉네임, null일 때 '익명')

  @Column(name = "is_anonymous", nullable = false)
  @Builder.Default
  private Boolean isAnonymous = false;

  /* Entity 필드를 primitive 타입(boolean) 대신 wrapper 타입(Boolean) 사용한 이유 :
  Fail-Fast 전략 (값 안 넣으면 null,제약조건에 걸림)*/

  @Column(name = "anonymous_email", nullable = true)
  private String anonymousEmail; // 익명 사용자 이메일 (익명 선택 시에만)

  @Column(name = "anonymous_pwd", nullable = true)
  private String anonymousPwd; // 익명 사용자 비밀번호 (익명 선택 시에만, 암호화)

  // 생성자 팩토리 메서드들

  /**
   * 회원 작성자 생성 auth-service에서 인증된 회원 정보로 작성자 엔티티를 생성
   *
   * @param userId 회원 ID (auth-service 참조값)
   * @param authorName 회원 이름
   * @return 회원 작성자 엔티티
   */
  public static Authors createMemberAuthor(Long userId, String authorName) {
    return Authors.builder().userId(userId).authorName(authorName).isAnonymous(false).build();
  }

  /**
   * 익명 작성자 생성 비회원이 게시글/댓글 작성 시 익명 정보로 작성자 엔티티를 생성
   *
   * @param email 익명 이메일 (수정/삭제 시 인증용)
   * @param encodedPassword 암호화된 패스워드 (수정/삭제 시 인증용)
   * @return 익명 작성자 엔티티
   */
  public static Authors createAnonymousAuthor(String email, String encodedPassword) {
    return Authors.builder()
        .authorName("익명")
        .isAnonymous(true)
        .anonymousEmail(email)
        .anonymousPwd(encodedPassword)
        .build();
  }

  // 비즈니스 메서드

  /**
   * 표시용 작성자명 반환 작성자명이 null인 경우 기본값 '익명'을 반환하는 안전한 접근자
   *
   * @return 표시할 작성자명 (null일 때 '익명')
   */
  public String getDisplayAuthorName() {
    return authorName != null ? authorName : "익명";
  }
}
