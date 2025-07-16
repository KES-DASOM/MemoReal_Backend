package com.dasom.MemoReal.UserTest;

import com.dasom.MemoReal.domain.UserManager.dto.UserRegisterRequest;
import com.dasom.MemoReal.domain.UserManager.entity.User;
import com.dasom.MemoReal.domain.UserManager.repository.UserRepository;
import com.dasom.MemoReal.domain.UserManager.service.UserService;
import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceTest.class);

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
            log.info("──────────── 회원가입 성공 테스트 시작 ────────────");

            UserRegisterRequest request = new UserRegisterRequest("user1", "user@example.com", "pass");

            when(repository.existsByUsername("user1")).thenReturn(false);
            when(repository.existsByEmail("user@example.com")).thenReturn(false);
            when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
            when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            User user = userService.register(request);

            log.info("✅ 회원가입 성공 - username: {}", user.getUsername());

            assertThat(user.getUsername()).isEqualTo("user1");
            assertThat(user.getPassword()).isEqualTo("encoded_pass");
        }

        @Test
        @DisplayName("이미 존재하는 사용자명")
        void register_duplicateUsername() {
            log.info("──────────── 사용자명 중복 테스트 시작 ────────────");

            UserRegisterRequest request = new UserRegisterRequest("user1", "user@example.com", "pass");
            when(repository.existsByUsername("user1")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME)
                    .satisfies(e -> {
                        log.warn("▶ 예외 메시지: {}", e.getMessage());
                        log.info("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }

        @Test
        @DisplayName("이미 존재하는 이메일")
        void register_duplicateEmail() {
            log.info("──────────── 이메일 중복 테스트 시작 ────────────");

            UserRegisterRequest request = new UserRegisterRequest("user1", "user@example.com", "pass");

            when(repository.existsByUsername("user1")).thenReturn(false);
            when(repository.existsByEmail("user@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL)
                    .satisfies(e -> {
                        log.warn("▶ 예외 메시지: {}", e.getMessage());
                        log.info("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("성공")
        void login_success() {
            log.info("──────────── 로그인 성공 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "encoded_pass");

            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("pass", "encoded_pass")).thenReturn(true);

            User result = userService.login("user@example.com", "pass");

            log.info("✅ 로그인 성공 - username: {}", result.getUsername());

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("user1");
        }

        @Test
        @DisplayName("이메일이 존재하지 않음")
        void login_with_nonexistent_email() {
            log.info("──────────── 로그인 실패 테스트 (이메일 없음) 시작 ────────────");

            when(repository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login("nonexistent@example.com", "any_password"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            log.warn("❌ 로그인 실패 - 이메일 없음으로 예외 발생");
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않음")
        void login_with_wrong_password() {
            log.info("──────────── 로그인 실패 테스트 (비밀번호 불일치) 시작 ────────────");

            User user = new User("user1", "user@example.com", "encoded_pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpass", "encoded_pass")).thenReturn(false);

            assertThatThrownBy(() -> userService.login("user@example.com", "wrongpass"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);

            log.warn("❌ 로그인 실패 - 비밀번호 불일치로 예외 발생");
        }
    }

    @Nested
    @DisplayName("유저 정보 수정 테스트")
    class UpdateUserInfoTest {

        @Test
        @DisplayName("username 수정 성공")
        void update_usernameChange() {
            log.info("──────────── username 수정 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            Map<String, Object> updates = Map.of("username", "newUser");

            User result = userService.updateUserInfoByMap("user@example.com", updates);

            log.info("✅ username 수정 성공 - username: {}", result.getUsername());

            assertThat(result.getUsername()).isEqualTo("newUser");
        }

        @Test
        @DisplayName("이미 존재하는 username으로 수정 시도")
        void update_duplicateUsername() {
            log.info("──────────── 이미 존재하는 username으로 수정 시도 테스트 시작 ────────────");

            User currentUser = new User("user1", "user@example.com", "pass");
            String existingUsername = "existingUser";

            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));
            when(repository.existsByUsername(existingUsername)).thenReturn(true);

            Map<String, Object> updates = Map.of("username", existingUsername);

            assertThatThrownBy(() -> userService.updateUserInfoByMap("user@example.com", updates))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME)
                    .satisfies(e -> {
                        log.warn("▶ 예외 메시지: {}", e.getMessage());
                        log.info("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }

        @Test
        @DisplayName("email 수정 시도")
        void update_emailChange() {
            log.info("──────────── email 수정 시도 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

            Map<String, Object> updates = Map.of("email", "new@example.com");

            assertThatThrownBy(() -> userService.updateUserInfoByMap("user@example.com", updates))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_UPDATE_FIELD)
                    .satisfies(e -> {
                        log.warn("▶ 예외 메시지: {}", e.getMessage());
                        log.info("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }

        @Test
        @DisplayName("password 수정 성공")
        void update_passwordChange() {
            log.info("──────────── password 수정 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "oldpass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newpass")).thenReturn("encoded_newpass");
            when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            Map<String, Object> updates = Map.of("password", "newpass");

            User result = userService.updateUserInfoByMap("user@example.com", updates);

            log.info("✅ password 수정 성공 - password: {}", result.getPassword());

            assertThat(result.getPassword()).isEqualTo("encoded_newpass");
        }

        @Test
        @DisplayName("허용되지 않은 필드 수정 시도")
        void update_invalidField() {
            log.info("──────────── 허용되지 않은 필드 수정 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

            Map<String, Object> updates = Map.of("role", "admin");

            assertThatThrownBy(() -> userService.updateUserInfoByMap("user@example.com", updates))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_UPDATE_FIELD)
                    .satisfies(e -> {
                        log.warn("▶ 예외 메시지: {}", e.getMessage());
                        log.info("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }

        @Test
        @DisplayName("사용자 없음")
        void update_userNotFound() {
            log.info("──────────── 사용자 없음 테스트 시작 ────────────");

            when(repository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            Map<String, Object> updates = Map.of("intro", "intro");

            assertThatThrownBy(() -> userService.updateUserInfoByMap("unknown@example.com", updates))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UPDATE_NOT_FOUND)
                    .satisfies(e -> {
                        log.warn("▶ 예외 메시지: {}", e.getMessage());
                        log.info("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }
    }
}
