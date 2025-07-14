package com.dasom.MemoReal.UserManager.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> { // 데이터 반환값 형식
    private boolean error;// 오류 발생여부
    private T data; // 데이터 or 오류 코드
}
