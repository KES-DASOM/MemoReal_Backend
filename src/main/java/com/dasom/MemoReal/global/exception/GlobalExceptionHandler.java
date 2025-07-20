package com.dasom.MemoReal.global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 사용자 정의 예외 처리 (캡슐 관련 포함)
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<?> handleCustomException(CustomException e) {
        ErrorCode code = e.getErrorCode();
        log.warn("CustomException occurred: {}", code.getMessage(), e);
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(Map.of(
                        "success", false,
                        "error", code.getMessage()
                ));
    }

    // 예기치 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleUnhandledException(Exception e) {
        log.error("Unhandled Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "error", "Internal Server Error"
                ));
    }
}