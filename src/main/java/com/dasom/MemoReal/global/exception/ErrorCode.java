package com.dasom.MemoReal.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 유저 등록, 수정
    DUPLICATE_USERNAME("USER_001", "이미 존재하는 사용자명입니다."),
    DUPLICATE_EMAIL("USER_002", "이미 존재하는 이메일입니다."),
    USER_UPDATE_NOT_FOUND("USER_003", "사용자를 찾을 수 없습니다."),
    EMAIL_UPDATE_FORBIDDEN("USER_004", "이메일은 수정할 수 없습니다."),
    INVALID_UPDATE_FIELD("USER_005", "수정할 수 없는 필드입니다."),


    // 사용자 로그인 관련 에러
    USER_NOT_FOUND("AUTH_001", "이메일이 존재하지 않습니다."),
    INVALID_PASSWORD("AUTH_002", "비밀번호가 일치하지 않습니다."),

    // 일반적인 에러(추후 유효성 검사 등 의 요인으로 사용)
    INVALID_INPUT_VALUE("COMMON_001", "유효하지 않은 입력 값입니다."),
    UNAUTHORIZED("COMMON_002", "인증되지 않은 접근입니다."),
    INTERNAL_SERVER_ERROR("COMMON_999", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}