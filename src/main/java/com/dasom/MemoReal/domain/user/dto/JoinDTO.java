package com.dasom.MemoReal.domain.user.dto;


import com.dasom.MemoReal.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinDTO {
    private String email;
    private String password;
    private String username;
    private List<String> roles = new ArrayList<>();

    public User toEntity(String encodedPassword, List<String> roles) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .roles(roles)
                .build();
    }
}
