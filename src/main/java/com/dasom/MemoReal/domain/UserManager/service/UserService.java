package com.dasom.MemoReal.domain.UserManager.service;

import com.dasom.MemoReal.domain.UserManager.dto.UserRegisterRequest;
import com.dasom.MemoReal.domain.UserManager.entity.User;
import com.dasom.MemoReal.domain.UserManager.repository.UserRepository;
import com.dasom.MemoReal.global.exception.ErrorCode;
import com.dasom.MemoReal.global.exception.BusinessException;
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
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(UserRegisterRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        return repository.save(request.toEntity(encodedPassword));
    }

    @Transactional(readOnly = true)
    public User login(String email, String rawPassword) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return user;
    }

    @Transactional
    public User updateUserInfoByMap(String email, Map<String, Object> updates) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_UPDATE_NOT_FOUND));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "username":
                    String newUsername = (String) value;
                    if (repository.existsByUsername(newUsername) && !user.getUsername().equals(newUsername)) {
                        throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
                    }
                    user.setUsername(newUsername);
                    break;
                case "email":
                    throw new BusinessException(ErrorCode.EMAIL_UPDATE_FORBIDDEN);
                case "password":
                    String encodedNewPassword = passwordEncoder.encode((String) value);
                    user.setPassword(encodedNewPassword);
                    break;
                default:
                    throw new BusinessException(ErrorCode.INVALID_UPDATE_FIELD, "수정할 수 없는 필드: " + key);
            }
        }

        return repository.save(user);
    }
}