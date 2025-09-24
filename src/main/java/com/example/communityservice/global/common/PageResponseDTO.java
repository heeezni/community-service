package com.example.communityservice.global.common;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 페이징 처리된 응답을 위한 공통 DTO. Spring Data Page 객체를 클라이언트 친화적 형태로 변환 <br>
 * 컨트롤러에서 Page<Post>를 그대로 반환하면 JSON 응답이 복잡하고 불필요한 정보가 많음 (클라이언트가 사용하기 어려움)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDTO<T> {

  private List<T> content; // 실제 데이터 목록
  private int page; // 현재 페이지 번호 (0부터 시작)
  private int size; // 페이지 크기
  private long totalElements; // 전체 데이터 개수
  private int totalPages; // 전체 페이지 수
  private boolean first; // 첫 번째 페이지 여부
  private boolean last; // 마지막 페이지 여부
  private boolean hasNext; // 다음 페이지 존재 여부
  private boolean hasPrevious; // 이전 페이지 존재 여부

  // from() : Spring Data JPA의 Page<T> 객체를 PageResponseDto로 변환하는 정적 팩토리 메서드
  public static <T> PageResponseDTO<T> from(Page<T> page) {
    return PageResponseDTO.<T>builder()
        .content(page.getContent())
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .first(page.isFirst())
        .last(page.isLast())
        .hasNext(page.hasNext())
        .hasPrevious(page.hasPrevious())
        .build();
  }
}
