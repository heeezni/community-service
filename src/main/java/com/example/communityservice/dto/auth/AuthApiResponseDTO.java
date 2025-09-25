package com.example.communityservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 인증 서비스 API 응답 래퍼 DTO <br>
 * - API 호출의 전반적인 결과(성공/실패) <br>
 * - 응답 메시지와 실제 데이터 페이로드 포함 <br>
 */
@Schema(description = "인증 서비스 API 응답")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthApiResponseDTO {

  @Schema(description = "응답 결과", example = "SUCCESS")
  private String result;

  @Schema(description = "응답 메시지", example = "사용자 정보 조회 성공")
  private String message;

  @Schema(description = "사용자 정보 데이터")
  private UserInfoResponseDTO data;

  /**
   * 응답이 성공인지 확인
   *
   * @return 성공 여부 (result가 "SUCCESS"인 경우 true)
   */
  public boolean isSuccess() {
    return "SUCCESS".equalsIgnoreCase(result); // 대소문자 무시하고 문자열 비교
  }
}
