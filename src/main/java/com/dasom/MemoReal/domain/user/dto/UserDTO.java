package com.dasom.MemoReal.domain.user.dto;


import com.dasom.MemoReal.domain.user.entity.User;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String username;

    static public UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public User toEntity() {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .build();
    }

}
