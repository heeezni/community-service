package com.example.communityservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.communityservice.entity.PostLikes;

/** PostLikes 엔티티 데이터베이스 접근 인터페이스 게시글 좋아요 관리 및 배치 조회 기능 제공 */
@Repository
public interface PostLikesRepository extends JpaRepository<PostLikes, Long> {

  // 사용자가 특정 게시글에 좋아요 눌렀는지 여부
  boolean existsByUserIdAndPostPostId(Long userId, Long postId);

  // 좋아요 삭제 (PostLikes 테이블에서 특정 사용자의 좋아요 기록 삭제)
  @Modifying
  @Query("DELETE FROM PostLikes pl WHERE pl.userId = :userId AND pl.post.postId = :postId")
  void deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

  /**
   * 배치 쿼리로 N+1 문제 해결 여러 게시글의 좋아요 상태를 한 번의 쿼리로 조회
   *
   * <p>일반적인 방식의 문제: - 게시글 10개 조회 → 각각 좋아요 체크 → 10번의 추가 쿼리 (N+1 문제)
   *
   * <p>배치 쿼리 방식: - IN 절을 사용해서 모든 게시글 ID를 한 번에 조회 → 1번의 쿼리 - 실행 SQL: SELECT post_id FROM post_likes
   * WHERE user_id = ? AND post_id IN (1,2,3,...)
   *
   * @param userId 사용자 ID
   * @param postIds 확인할 게시글 ID 목록
   * @return 사용자가 좋아요한 게시글 ID 목록
   */
  // 특정 게시물 목록 중에서 사용자가 좋아요를 누른 게시물들의 ID를 찾는 메서드 (게시물 목록 UI에서 각 게시물마다 좋아요 여부에 따른 ♥/♡ 표시를 하려면 필요)
  @Query(
      "SELECT pl.post.postId FROM PostLikes pl WHERE pl.userId = :userId AND pl.post.postId IN :postIds")
  List<Long> findLikedPostIdsByUserIdAndPostIds(
      @Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
