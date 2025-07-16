package com.dasom.MemoReal.UserTest;

import com.dasom.MemoReal.domain.UserManager.dto.UserRegisterRequest;
import com.dasom.MemoReal.domain.UserManager.entity.User;
import com.dasom.MemoReal.domain.UserManager.repository.UserRepository;
import com.dasom.MemoReal.domain.UserManager.service.UserService;
import com.dasom.MemoReal.global.exception.ErrorCode; // 기존 import (패키지명 변경됨)
import com.dasom.MemoReal.global.exception.BusinessException; // 기존 import (패키지명 변경됨)
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(repository, passwordEncoder);
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("성공")
        void register_success() {
            System.out.println("──────────── 회원가입 성공 테스트 시작 ────────────");

            UserRegisterRequest request = new UserRegisterRequest("user1", "user@example.com", "pass");

            when(repository.existsByUsername("user1")).thenReturn(false);
            when(repository.existsByEmail("user@example.com")).thenReturn(false);
            when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
            when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            User user = userService.register(request);

            System.out.println("✅ 회원가입 성공 - username: " + user.getUsername());

            assertThat(user.getUsername()).isEqualTo("user1");
            assertThat(user.getPassword()).isEqualTo("encoded_pass");
        }

        @Test
        @DisplayName("이미 존재하는 사용자명")
        void register_duplicateUsername() {
            System.out.println("──────────── 사용자명 중복 테스트 시작 ────────────");

            UserRegisterRequest request = new UserRegisterRequest("user1", "user@example.com", "pass");
            when(repository.existsByUsername("user1")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME)
                    .satisfies(e -> {
                        System.out.println("▶ 예외 메시지: " + e.getMessage());
                        System.out.println("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }

        @Test
        @DisplayName("이미 존재하는 이메일")
        void register_duplicateEmail() {
            System.out.println("──────────── 이메일 중복 테스트 시작 ────────────");

            UserRegisterRequest request = new UserRegisterRequest("user1", "user@example.com", "pass");

            when(repository.existsByUsername("user1")).thenReturn(false);
            when(repository.existsByEmail("user@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL)
                    .satisfies(e -> {
                        System.out.println("▶ 예외 메시지: " + e.getMessage());
                        System.out.println("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("성공")
        void login_success() {
            System.out.println("──────────── 로그인 성공 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "encoded_pass");

            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("pass", "encoded_pass")).thenReturn(true);

            User result = userService.login("user@example.com", "pass");

            System.out.println("✅ 로그인 성공 - username: " + result.getUsername());

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("user1");
        }

        @Test
        @DisplayName("이메일이 존재하지 않음")
        void login_with_nonexistent_email() {
            System.out.println("──────────── 로그인 실패 테스트 (이메일 없음) 시작 ────────────");

            // given
            when(repository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login("nonexistent@example.com", "any_password"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            System.out.println("❌ 로그인 실패 - 이메일 없음으로 예외 발생");
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않음")
        void login_with_wrong_password() {
            System.out.println("──────────── 로그인 실패 테스트 (비밀번호 불일치) 시작 ────────────");

            // given
            User user = new User("user1", "user@example.com", "encoded_pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpass", "encoded_pass")).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login("user@example.com", "wrongpass"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);

            System.out.println("❌ 로그인 실패 - 비밀번호 불일치로 예외 발생");
        }

    }

    @Nested
    @DisplayName("유저 정보 수정 테스트")
    class UpdateUserInfoTest {

        @Test
        @DisplayName("username 수정 성공")
        void update_usernameChange() {
            System.out.println("──────────── username 수정 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            Map<String, Object> updates = Map.of("username", "newUser");

            User result = userService.updateUserInfoByMap("user@example.com", updates);

            System.out.println("✅ username 수정 성공 - username: " + result.getUsername());

            assertThat(result.getUsername()).isEqualTo("newUser");
        }

        // --- 새로 추가된 테스트 케이스 ---
        @Test
        @DisplayName("이미 존재하는 username으로 수정 시도")
        void update_duplicateUsername() {
            System.out.println("──────────── 이미 존재하는 username으로 수정 시도 테스트 시작 ────────────");

            User currentUser = new User("user1", "user@example.com", "pass");
            String existingUsername = "existingUser"; // 이미 DB에 있는 사용자명 가정

            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));
            when(repository.existsByUsername(existingUsername)).thenReturn(true); // 이미 존재한다고 가정

            Map<String, Object> updates = Map.of("username", existingUsername);

            assertThatThrownBy(() -> userService.updateUserInfoByMap("user@example.com", updates))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME)
                    .satisfies(e -> {
                        System.out.println("▶ 예외 메시지: " + e.getMessage());
                        System.out.println("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }
        // --- 새로 추가된 테스트 케이스 끝 ---


        @Test
        @DisplayName("email 수정 시도")
        void update_emailChange() {
            System.out.println("──────────── email 수정 시도 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

            Map<String, Object> updates = Map.of("email", "new@example.com");

            assertThatThrownBy(() -> userService.updateUserInfoByMap("user@example.com", updates))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_UPDATE_FORBIDDEN)
                    .satisfies(e -> {
                        System.out.println("▶ 예외 메시지: " + e.getMessage());
                        System.out.println("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }

        @Test
        @DisplayName("password 수정 성공")
        void update_passwordChange() {
            System.out.println("──────────── password 수정 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "oldpass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newpass")).thenReturn("encoded_newpass");
            when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            Map<String, Object> updates = Map.of("password", "newpass");

            User result = userService.updateUserInfoByMap("user@example.com", updates);

            System.out.println("✅ password 수정 성공 - password: " + result.getPassword());

            assertThat(result.getPassword()).isEqualTo("encoded_newpass");
        }

        @Test
        @DisplayName("허용되지 않은 필드 수정 시도")
        void update_invalidField() {
            System.out.println("──────────── 허용되지 않은 필드 수정 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

            Map<String, Object> updates = Map.of("role", "admin");

            assertThatThrownBy(() -> userService.updateUserInfoByMap("user@example.com", updates))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_UPDATE_FIELD)
                    .satisfies(e -> {
                        System.out.println("▶ 예외 메시지: " + e.getMessage());
                        System.out.println("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }

        @Test
        @DisplayName("사용자 없음")
        void update_userNotFound() {
            System.out.println("──────────── 사용자 없음 테스트 시작 ────────────");

            when(repository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            Map<String, Object> updates = Map.of("intro", "intro");

            assertThatThrownBy(() -> userService.updateUserInfoByMap("unknown@example.com", updates))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UPDATE_NOT_FOUND)
                    .satisfies(e -> {
                        System.out.println("▶ 예외 메시지: " + e.getMessage());
                        System.out.println("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }
    }

}