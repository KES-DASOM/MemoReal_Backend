package com.dasom.MemoReal.UserManager.domain.dto;
import com.dasom.MemoReal.UserManager.domain.entity.User;
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
