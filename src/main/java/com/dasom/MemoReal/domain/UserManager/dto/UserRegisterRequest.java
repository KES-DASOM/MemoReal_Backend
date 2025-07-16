package com.dasom.MemoReal.domain.UserManager.dto;
import com.dasom.MemoReal.domain.UserManager.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
    private String username;
    private String email;
    private String password;

    public User toEntity(String encodedPassword) {
        return new User(username, email, encodedPassword);
    }
}
