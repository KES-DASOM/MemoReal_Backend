package com.dasom.MemoReal.domain.UserManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> { // 데이터 반환값 형식
    private boolean success;// 성공 여부
    private T data; // 데이터 or 오류 코드
}
