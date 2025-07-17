package com.dasom.MemoReal.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 사용자 정의 예외 처리 (비즈니스, 서버 예외)
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<?> handleCustomException(CustomException e) {
        ErrorCode code = e.getErrorCode();
        HttpStatus status = code.getHttpStatus();
        log.warn("CustomException occurred: {}", code.getMessage(), e);
        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "success", false,
                        "error", code.getMessage()
                )); // 오류라 success=false와 에러 메시지 전달 // 오류라 success=false
    }

    // 예기치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception e) {
        log.error("Unhandled Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(Map.of(
                        "success", false,
                        "error", "Internal Server Error"
                ));// 오류라 success=false
    }

}