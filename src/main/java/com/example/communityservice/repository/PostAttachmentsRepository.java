package com.example.communityservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.communityservice.entity.PostAttachments;

/** 첨부파일 관련 데이터베이스 접근 리포지토리 */
@Repository
public interface PostAttachmentsRepository extends JpaRepository<PostAttachments, Long> {

  /** 특정 게시글의 첨부파일 목록 조회 */
  @Query("SELECT pa FROM PostAttachments pa WHERE pa.post.postId = :postId")
  List<PostAttachments> findByPostId(@Param("postId") Long postId);

  /** 특정 게시글의 첨부파일 개수 조회 */
  @Query("SELECT COUNT(pa) FROM PostAttachments pa WHERE pa.post.postId = :postId")
  int countByPostId(@Param("postId") Long postId);

  // 특정 게시글의 첨부파일 삭제 메서드 구현 X : Posts 엔티티의 cascade 설정에 따라 연결된 첨부파일, 댓글 등의 DB 레코드는 자동으로 삭제됨
}
