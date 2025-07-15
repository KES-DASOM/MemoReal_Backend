package com.dasom.MemoReal.UserTest;

import com.dasom.MemoReal.UserManager.domain.dto.UserRegisterRequest;
import com.dasom.MemoReal.UserManager.domain.entity.User;
import com.dasom.MemoReal.UserManager.domain.repository.UserRepository;
import com.dasom.MemoReal.UserManager.domain.service.UserService;
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
        userService = new UserService(repository,passwordEncoder);
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
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 존재하는 사용자명입니다.")
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
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 존재하는 이메일입니다.")
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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일이 존재하지 않습니다.");

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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비밀번호가 일치하지 않습니다.");

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

        @Test
        @DisplayName("email 수정 시도")
        void update_emailChange() {
            System.out.println("──────────── email 수정 시도 테스트 시작 ────────────");

            User user = new User("user1", "user@example.com", "pass");
            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

            Map<String, Object> updates = Map.of("email", "new@example.com");

            assertThatThrownBy(() -> userService.updateUserInfoByMap("user@example.com", updates))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일은 수정할 수 없습니다.")
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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("수정할 수 없는 필드: role")
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
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.")
                    .satisfies(e -> {
                        System.out.println("▶ 예외 메시지: " + e.getMessage());
                        System.out.println("✅ 예외가 정상적으로 처리되었습니다.");
                    });
        }
    }

}
