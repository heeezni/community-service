package com.example.communityservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.communityservice.entity.Authors;

/** Authors 엔티티 데이터베이스 접근 인터페이스 회원 작성자 조회 기능만 제공 (익명은 각 게시글/댓글마다 개별 생성) */
@Repository
public interface AuthorsRepository extends JpaRepository<Authors, Long> {

  Optional<Authors> findByUserId(Long userId);

  // 암호화된 비밀번호는 데이터베이스에서 직접 비교 불가 ➡ passwordEncoder.matches() 사용하기
}
