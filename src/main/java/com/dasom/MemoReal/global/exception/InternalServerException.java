package com.dasom.MemoReal.global.exception;

import lombok.Getter;

// 500 Internal Server Error에 해당하는 서버 내부 에러(DB 입출력, ipfs에 캡슐 저장 등의 과정에서 오류 발생 시 예외처리 로 사용 할 예정)
@Getter
public class InternalServerException extends RuntimeException {
    private final ErrorCode errorCode;

    public InternalServerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public InternalServerException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
