package com.example.communityservice.global.exception;

import lombok.Getter;

/** 비즈니스 로직 관련 예외의 최상위 클래스 ErrorCode를 포함하여 일관된 예외 처리 제공 */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BusinessException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
