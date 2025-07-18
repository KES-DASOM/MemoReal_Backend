package com.dasom.MemoReal.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 유저 등록, 수정
    DUPLICATE_USERNAME("USER_001", HttpStatus.CONFLICT, "이미 존재하는 사용자명입니다."),
    DUPLICATE_EMAIL("USER_002", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    USER_UPDATE_NOT_FOUND("USER_003", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_UPDATE_FIELD("USER_004", HttpStatus.BAD_REQUEST, "수정할 수 없는 필드입니다."),
    USER_ID_NOT_FOUND("USER_005", HttpStatus.NOT_FOUND, "유저 아이디를 찾을 수 없습니다."),

    // 사용자 로그인 관련 에러
    USER_NOT_FOUND("AUTH_001", HttpStatus.NOT_FOUND, "이메일이 존재하지 않습니다."),
    INVALID_PASSWORD("AUTH_002", HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // 일반적인 에러(유효성 검사 등)
    INVALID_INPUT_VALUE("COMMON_001", HttpStatus.BAD_REQUEST, "유효하지 않은 입력 값입니다."),
    UNAUTHORIZED("COMMON_002", HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."),
    INTERNAL_SERVER_ERROR("COMMON_999", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 콘텐츠 관련 에러
    METADATA_NOT_FOUND("CONTENT_001", HttpStatus.NOT_FOUND, "메타데이터를 찾을 수 없습니다."),
    FILE_NOT_FOUND("CONTENT_002", HttpStatus.NOT_FOUND, "요청한 파일을 찾을 수 없습니다."),
    ACCESS_DENIED("CONTENT_003", HttpStatus.FORBIDDEN, "열람 조건을 만족하지 못했습니다."),
    UPLOAD_FAILED("CONTENT_004", HttpStatus.INTERNAL_SERVER_ERROR, "컨텐츠 업로드에 실패했습니다."),
    INVALID_INPUT("CONTENT_005", HttpStatus.BAD_REQUEST, "유효하지 않은 입력 값입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
