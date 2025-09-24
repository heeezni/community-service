package com.example.communityservice.entity;

import lombok.Getter;

/**
 * 게시글 카테고리 Enum EnumType.STRING 사용 이유: - ORDINAL은 순서(0,1,2...)로 저장되어 Enum 순서 변경 시 데이터 무결성 문제 발생 -
 * STRING은 실제 Enum 이름으로 저장되어 안전하고 가독성이 좋음 - 데이터베이스에서 직접 확인 가능하여 디버깅과 유지보수에 유리
 */
@Getter
public enum PostCategory {
  FREE_BOARD("자유게시판"),
  PRICE_INFO("가격정보"),
  LIQUOR_REVIEW("술리뷰"),
  QNA("질문답변"),
  EVENT("이벤트");

  private final String displayName; // 한글 이름 저장할 곳

  PostCategory(String displayName) {
    this.displayName = displayName;
  }
}
