package com.dasom.MemoReal.global.exception;

import com.dasom.MemoReal.domain.UserManager.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // BusinessException 처리 (예: 중복 사용자, 유효하지 않은 비밀번호)
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<CommonResponse<?>> handleBusinessException(BusinessException e) {
        log.warn("Business Exception Occurred: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new CommonResponse<>(true, e.getErrorCode().getMessage()),
                HttpStatus.BAD_REQUEST // 클라이언트 오류에 대해 400 Bad Request 반환
        );
    }

    // InternalServerException 처리
    @ExceptionHandler(InternalServerException.class)
    protected ResponseEntity<CommonResponse<?>> handleInternalServerException(InternalServerException e) {
        log.error("Internal Server Exception Occurred: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new CommonResponse<>(true, ErrorCode.INTERNAL_SERVER_ERROR.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR // 서버 오류에 대해 500 Internal Server Error 반환
        );
    }

    // 일반적인 예외(예상치 못한 에러) 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<CommonResponse<?>> handleGeneralException(Exception e) {
        log.error("Unexpected Internal Server Error: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new CommonResponse<>(true, ErrorCode.INTERNAL_SERVER_ERROR.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // Spring의 유효성 검사 예외 (예: @Valid 사용 시) 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CommonResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.warn("Validation Exception Occurred: {}", errorMessage, e);
        return new ResponseEntity<>(
                new CommonResponse<>(true, errorMessage != null ? errorMessage : ErrorCode.INVALID_INPUT_VALUE.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }
}