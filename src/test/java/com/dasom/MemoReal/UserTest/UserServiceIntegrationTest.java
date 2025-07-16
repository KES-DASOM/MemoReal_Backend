package com.dasom.MemoReal.UserTest;

import com.dasom.MemoReal.domain.UserManager.dto.UserRegisterRequest;
import com.dasom.MemoReal.domain.UserManager.entity.User;
import com.dasom.MemoReal.domain.UserManager.repository.UserRepository;
import com.dasom.MemoReal.domain.UserManager.service.UserService;
import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceIntegrationTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "integrationTestUser";
    private static final String TEST_EMAIL = "integration@example.com";
    private static final String TEST_PASSWORD = "integrationPass";

    @BeforeEach
    void cleanupBeforeEach() {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        userRepository.flush();
        log.info("BeforeEach: 이전 테스트 데이터 (이메일: {}) 정리 완료", TEST_EMAIL);
    }

    @Test
    @Order(1)
    @Transactional
    @DisplayName("✅ 1. 실제 DB에 유저 등록 및 조회 테스트")
    void registerAndFindUserInRealDB() {
        log.info("---- [1] 유저 등록 및 조회 테스트 시작 ----");

        UserRegisterRequest request = new UserRegisterRequest(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD);
        User savedUser = userService.register(request);
        log.info("유저 등록 완료. User ID: {}, Username: {}", savedUser.getUid(), savedUser.getUsername());

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(passwordEncoder.matches(TEST_PASSWORD, savedUser.getPassword())).isTrue();

        Optional<User> foundUserOptional = userRepository.findByUsername(TEST_USERNAME);
        assertThat(foundUserOptional).isPresent();
        User foundUser = foundUserOptional.get();

        assertThat(foundUser.getUid()).isEqualTo(savedUser.getUid());
        assertThat(foundUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(passwordEncoder.matches(TEST_PASSWORD, foundUser.getPassword())).isTrue();
        log.info("유저 DB 조회 및 검증 완료. Found User ID: {}", foundUser.getUid());

        log.info("---- [1] 유저 등록 및 조회 테스트 종료 ----\n");
    }

    @Test
    @Order(2)
    @Transactional
    @DisplayName("❌ 2. 이미 존재하는 유저명으로 등록 시도 - CustomException 발생")
    void registerUserWithDuplicateUsername() {
        log.info("---- [2] 중복 유저명 등록 테스트 시작 ----");

        userService.register(new UserRegisterRequest(TEST_USERNAME, "unique@example.com", "pass"));

        UserRegisterRequest duplicateRequest = new UserRegisterRequest(TEST_USERNAME, "another@example.com", "anotherpass");
        assertThatThrownBy(() -> userService.register(duplicateRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME);

        log.warn("중복 유저명 등록 시도 -> CustomException(DUPLICATE_USERNAME) 발생 확인.");
        log.info("---- [2] 중복 유저명 등록 테스트 종료 ----\n");
    }

    @Test
    @Order(3)
    @Transactional
    @DisplayName("❌ 3. 이미 존재하는 이메일로 등록 시도 - CustomException 발생")
    void registerUserWithDuplicateEmail() {
        log.info("---- [3] 중복 이메일 등록 테스트 시작 ----");

        userService.register(new UserRegisterRequest("anotherUser", TEST_EMAIL, "pass"));

        UserRegisterRequest duplicateRequest = new UserRegisterRequest("anotherUser2", TEST_EMAIL, "anotherpass");
        assertThatThrownBy(() -> userService.register(duplicateRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        log.warn("중복 이메일 등록 시도 -> CustomException(DUPLICATE_EMAIL) 발생 확인.");
        log.info("---- [3] 중복 이메일 등록 테스트 종료 ----\n");
    }

    @Test
    @Order(4)
    @DisplayName("✅ 4. 로그인 기능 테스트")
    void loginUserTest() {
        log.info("---- [4] 로그인 테스트 시작 ----");

        userService.register(new UserRegisterRequest(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD));
        User loggedInUser = userService.login(TEST_EMAIL, TEST_PASSWORD);
        log.info("로그인 성공. User Email: {}", loggedInUser.getEmail());

        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(passwordEncoder.matches(TEST_PASSWORD, loggedInUser.getPassword())).isTrue();

        log.info("---- [4] 로그인 테스트 종료 ----\n");
    }

    @Test
    @Order(5)
    @DisplayName("❌ 5. 존재하지 않는 이메일로 로그인 시도 - CustomException 발생")
    void loginWithNonExistentEmail() {
        log.info("---- [5] 존재하지 않는 이메일로 로그인 테스트 시작 ----");

        assertThatThrownBy(() -> userService.login("nonexistent@example.com", "anypass"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        log.warn("존재하지 않는 이메일 로그인 시도 -> CustomException(USER_NOT_FOUND) 발생 확인.");
        log.info("---- [5] 존재하지 않는 이메일로 로그인 테스트 종료 ----\n");
    }

    @Test
    @Order(6)
    @DisplayName("❌ 6. 잘못된 비밀번호로 로그인 시도 - CustomException 발생")
    void loginWithWrongPassword() {
        log.info("---- [6] 잘못된 비밀번호로 로그인 테스트 시작 ----");

        userService.register(new UserRegisterRequest(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD));

        assertThatThrownBy(() -> userService.login(TEST_EMAIL, "wrongpass"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);

        log.warn("잘못된 비밀번호 로그인 시도 -> CustomException(INVALID_PASSWORD) 발생 확인.");
        log.info("---- [6] 잘못된 비밀번호로 로그인 테스트 종료 ----\n");
    }

    @Test
    @Order(7)
    @Transactional
    @DisplayName("✅ 7. 회원정보 수정 테스트 (username, password 변경)")
    void updateUserInfoTest() {
        log.info("---- [7] 회원정보 수정 테스트 시작 ----");

        userService.register(new UserRegisterRequest(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD));

        String newUsername = "updatedUserIntegration";
        String newPassword = "newIntegrationPass";

        Map<String, Object> updates = Map.of("username", newUsername, "password", newPassword);
        User updatedUser = userService.updateUserInfoByMap(TEST_EMAIL, updates);

        assertThat(updatedUser.getUsername()).isEqualTo(newUsername);
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();

        log.info("회원정보 수정 결과 확인 완료.");
        log.info("---- [7] 회원정보 수정 테스트 종료 ----\n");
    }

    @Test
    @Order(8)
    @Transactional
    @DisplayName("❌ 8. 이미 존재하는 username으로 수정 시도 - CustomException 발생")
    void updateUserInfoWithDuplicateUsername() {
        log.info("---- [8] 중복 username으로 수정 시도 테스트 시작 ----");

        String dummyUsername = "dummyUser";
        String dummyEmail = "dummy@example.com";
        userRepository.save(new User(dummyUsername, dummyEmail, passwordEncoder.encode("dummy")));

        userService.register(new UserRegisterRequest(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD));

        Map<String, Object> updates = Map.of("username", dummyUsername);
        assertThatThrownBy(() -> userService.updateUserInfoByMap(TEST_EMAIL, updates))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME);

        log.warn("중복 username으로 수정 시도 -> CustomException(DUPLICATE_USERNAME) 발생 확인.");

        userRepository.findByEmail(dummyEmail).ifPresent(userRepository::delete);
        log.info("---- [8] 중복 username으로 수정 시도 테스트 종료 ----\n");
    }

    @Test
    @Order(9)
    @Transactional
    @DisplayName("❌ 9. 이메일 수정 시도 - CustomException 발생")
    void updateUserInfoEmailForbidden() {
        log.info("---- [9] 이메일 수정 시도 테스트 시작 ----");

        userService.register(new UserRegisterRequest(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD));

        Map<String, Object> updates = Map.of("email", "newemail@example.com");
        assertThatThrownBy(() -> userService.updateUserInfoByMap(TEST_EMAIL, updates))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_UPDATE_FIELD);

        log.warn("이메일 수정 시도 -> CustomException(INVALID_UPDATE_FIELD) 발생 확인.");
        log.info("---- [9] 이메일 수정 시도 테스트 종료 ----\n");
    }

    @Test
    @Order(10)
    @Transactional
    @DisplayName("❌ 10. 허용되지 않은 필드 수정 시도 - CustomException 발생")
    void updateUserInfoInvalidField() {
        log.info("---- [10] 허용되지 않은 필드 수정 시도 테스트 시작 ----");

        userService.register(new UserRegisterRequest(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD));

        Map<String, Object> updates = Map.of("role", "admin");
        assertThatThrownBy(() -> userService.updateUserInfoByMap(TEST_EMAIL, updates))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_UPDATE_FIELD);

        log.warn("허용되지 않은 필드 수정 시도 -> CustomException(INVALID_UPDATE_FIELD) 발생 확인.");
        log.info("---- [10] 허용되지 않은 필드 수정 시도 테스트 종료 ----\n");
    }

    @Test
    @Order(11)
    @Transactional
    @DisplayName("❌ 11. 존재하지 않는 사용자의 정보 수정 시도 - CustomException 발생")
    void updateUserInfoUserNotFound() {
        log.info("---- [11] 존재하지 않는 사용자 정보 수정 테스트 시작 ----");

        Map<String, Object> updates = Map.of("username", "newname");
        assertThatThrownBy(() -> userService.updateUserInfoByMap("notfound@example.com", updates))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UPDATE_NOT_FOUND);

        log.warn("존재하지 않는 사용자 정보 수정 시도 -> CustomException(USER_UPDATE_NOT_FOUND) 발생 확인.");
        log.info("---- [11] 존재하지 않는 사용자 정보 수정 테스트 종료 ----\n");
    }

    @Test
    @Order(12)
    @Transactional
    @DisplayName("🧹 12. 테스트 유저 데이터 정리")
    void cleanupTestUser() {
        log.info("---- [12] 테스트 유저 데이터 정리 시작 ----");

        userRepository.findByEmail(TEST_EMAIL).ifPresent(user -> {
            userRepository.delete(user);
            log.info("🗑️ 테스트 유저 삭제 완료 - email: {}", user.getEmail());
        });

        boolean exists = userRepository.existsByEmail(TEST_EMAIL);
        assertThat(exists).isFalse();
        log.info("삭제 후 사용자 존재 여부: {}", exists);

        log.info("---- [12] 테스트 유저 데이터 정리 종료 ----\n");
    }
}
