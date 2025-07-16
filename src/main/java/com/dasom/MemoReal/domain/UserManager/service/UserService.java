package com.dasom.MemoReal.domain.UserManager.service;

import com.dasom.MemoReal.domain.UserManager.dto.UserRegisterRequest;
import com.dasom.MemoReal.domain.UserManager.entity.User;
import com.dasom.MemoReal.domain.UserManager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder; // 암호화기 초기화
    }

    @Transactional
    public User register(UserRegisterRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("이미 존재하는 사용자명입니다.");
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        return repository.save(request.toEntity(encodedPassword));
    }

    @Transactional(readOnly = true)
    public User login(String email, String rawPassword) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    @Transactional
    public User updateUserInfoByMap(String email, Map<String, Object> updates) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "username":
                    user.setUsername((String) value);
                    break;
                case "email":
                    throw new IllegalArgumentException("이메일은 수정할 수 없습니다.");
                case "password":
                    String encodedNewPassword = passwordEncoder.encode((String) value);
                    user.setPassword(encodedNewPassword);
                    break;
                default:
                    throw new IllegalArgumentException("수정할 수 없는 필드: " + key);
            }
        }

        return repository.save(user);
    }
}
