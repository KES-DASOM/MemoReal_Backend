package com.dasom.MemoReal.UserManager.domain.dto;

import com.dasom.MemoReal.UserManager.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String jwt;
}
