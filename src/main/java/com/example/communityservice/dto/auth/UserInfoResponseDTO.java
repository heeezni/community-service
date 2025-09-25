package com.example.communityservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 인증 서비스에서 받은 사용자 정보 DTO <br>
 * - 사용자 식별에 필요한 기본 정보 <br>
 * - ID, 사용자명, 이메일 포함 <br>
 */
@Schema(description = "사용자 정보 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // JSON ➡ Java 객체 변환할 때, 객체에 없는 알 수 없는 필드가 있으면 무시하도록 설정
public class UserInfoResponseDTO {

  @Schema(description = "사용자 ID", example = "1")
  private Long id;

  @Schema(description = "사용자명", example = "홍길동")
  private String username;

  @Schema(description = "이메일", example = "hong@example.com")
  private String email;
}
