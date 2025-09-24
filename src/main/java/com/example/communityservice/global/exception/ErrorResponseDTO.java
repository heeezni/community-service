package com.example.communityservice.global.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 에러 응답용 DTO. 클라이언트에게 일관된 에러 응답 형식 제공 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

  private boolean success; // 성공 여부 (항상 false)
  private String code; // 에러 코드 (예: "POST_ACCESS_DENIED")
  private String message; // 에러 메세지 (예: "작성자를 찾을 수 없습니다.")
  private LocalDateTime timestamp; // 에러 발생 시간

  public static ErrorResponseDTO of(
      ErrorCode errorCode) { // 정적 팩토리메서드 of() : A of B ➡ "B로 부터 A를 만들어라"
    return ErrorResponseDTO.builder()
        .success(false)
        .code(errorCode.getCode()) // ErrorCode에서 코드 가져옴
        .message(errorCode.getMessage()) // ErrorCode에서 메세지 가져옴
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static ErrorResponseDTO of(ErrorCode errorCode, String message) {
    return ErrorResponseDTO.builder()
        .success(false)
        .code(errorCode.getCode())
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static ErrorResponseDTO of(String code, String message) {
    return ErrorResponseDTO.builder()
        .success(false)
        .code(code)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }
}
