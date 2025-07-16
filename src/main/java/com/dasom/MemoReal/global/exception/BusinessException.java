package com.dasom.MemoReal.global.exception;

import lombok.Getter;

// 400 Bad Request에 해당하는 비즈니스 로직 에러
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
    }// 에러코드는 공유하되 메시지를 커스텀 할 때 사용

}

