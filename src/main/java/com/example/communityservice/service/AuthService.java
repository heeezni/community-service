package com.example.communityservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.communityservice.dto.auth.AuthApiResponseDTO;
import com.example.communityservice.dto.auth.UserInfoResponseDTO;
import com.example.communityservice.global.exception.BusinessException;
import com.example.communityservice.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 관련 비즈니스 로직 처리 서비스 <br>
 * - 외부 인증 서비스와 통신 <br>
 * - 토큰 유효성 검증 및 사용자 정보 조회 <br>
 * - MSA 환경에서의 인증 연동
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
  // 다른 서버(API)와 HTTP 통신할 때 사용하는 스프링 제공 클래스
  private final RestTemplate restTemplate = new RestTemplate();
  // JSON 문자열 ↔ 자바 객체 변환을 담당하는 Jackson 라이브러리 (자동 역직렬화 가능하지만, 래퍼 구조 처리, 로깅, 예외 제어 위해 사용)
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${auth.service.url}")
  private String authServiceUrl;

  /**
   * Authorization 헤더를 통해 토큰 검증 & 사용자 정보 조회
   *
   * @param authorizationHeader Authorization 헤더 값 (Bearer token)
   * @return 사용자 정보 (userId, username 등)
   */
  public UserInfoResponseDTO validateTokenAndGetUser(String authorizationHeader) {
    String fullUrl = authServiceUrl + "/api/v1/auth/me";
    log.info("인증 서비스 호출 URL: {}", fullUrl);
    log.info("인증 헤더: {}", authorizationHeader);

    try {
      // HTTP 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", authorizationHeader); // Authorization: Bearer 토큰값
      headers.set("Content-Type", "application/json"); // Content-Type: application/json
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response =
          // restTemplate:  Spring에서 제공하는 HTTP 클라이언트 객체, REST API를 호출할 때 주로 사용
          restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
      // exchange(요청 보낼 URL, HTTP 메서드, 요청에 담을 헤더와 바디, 응답 본문을 어떤 타입으로 받을지)

      log.info("HttpEntity의 Headers: {}", response.getHeaders());
      log.info("HttpEntity의 Body: {}", response.getBody());

      // JSON 응답을 AuthApiResponseDTO로 변환
      AuthApiResponseDTO apiResponseDTO =
          objectMapper.readValue(response.getBody(), AuthApiResponseDTO.class);

      if (response.getStatusCode().is2xxSuccessful() && apiResponseDTO.isSuccess()) {
        // HTTP 상태 코드가 200~299 범위이고, apiResponse.isSuccess()가 true일 때만 성공
        return apiResponseDTO.getData(); // 사용자 정보 반환
      } else {
        log.error(
            "인증 서비스 응답 실패. 상태: {}, 메시지: {}", response.getStatusCode(), apiResponseDTO.getMessage());
        throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
      }

    } catch (RestClientException e) {
      log.error("인증 서비스 호출 실패: ", e);
      throw new BusinessException(ErrorCode.AUTH_SERVICE_ERROR);
    } catch (Exception e) {
      log.error("인증 응답 처리 실패: ", e);
      throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
    }
  }

  /**
   * Authorization 헤더에서 Bearer 토큰 추출
   *
   * @param authorizationHeader Authorization 헤더
   * @return 토큰 문자열 (Bearer 접두사 제거)
   */
  public String extractToken(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new BusinessException(
          ErrorCode.AUTHENTICATION_FAILED, "올바르지 않은 Authorization 헤더 형식입니다.");
    }
    return authorizationHeader.substring(7); // "Bearer " 제거 후 토큰만 추출
  }
}
