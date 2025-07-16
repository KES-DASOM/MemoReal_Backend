package com.dasom.MemoReal.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public CustomException(ErrorCode errorCode, String customMessage) {
    super(customMessage);
    this.errorCode = errorCode;
  }// 에러코드는 공유하되 메시지 커스텀 해서 사용
}
