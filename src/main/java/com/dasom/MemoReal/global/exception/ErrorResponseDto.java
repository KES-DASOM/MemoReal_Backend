package com.dasom.MemoReal.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private String code;
    private String message;

    public static ErrorResponseDto from(ErrorCode errorCode) {
        return new ErrorResponseDto(errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponseDto from(CustomException ex) {
        return new ErrorResponseDto(ex.getErrorCode().getCode(), ex.getMessage());
    }
}
