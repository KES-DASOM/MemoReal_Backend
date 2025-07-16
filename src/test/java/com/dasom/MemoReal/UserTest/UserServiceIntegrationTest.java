package com.dasom.MemoReal.UserTest;

import com.dasom.MemoReal.domain.UserManager.dto.UserRegisterRequest;
import com.dasom.MemoReal.domain.UserManager.entity.User;
import com.dasom.MemoReal.domain.UserManager.repository.UserRepository;
import com.dasom.MemoReal.domain.UserManager.service.UserService;
import com.dasom.MemoReal.global.exception.ErrorCode;
import com.dasom.MemoReal.global.exception.BusinessException;
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

    @BeforeAll
    static void setup(@Autowired UserRepository userRepository) {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        log.info("BeforeAll: 이전 테스트 데이터 (이메일: {}) 정리 완료", TEST_EMAIL);
    }

    @Test
    @Order(1)
    @DisplayName("✅ 1. 실제 DB에 유저 등록 및 조회 테스트")
    void registerAndFindUserInRealDB() {
        log.info("---- [1] 유저 등록 및 조회 테스트 시작 ----");

        // given
        UserRegisterRequest request = new UserRegisterRequest(
                TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD
        );

        // when
        User savedUser = userService.register(request);
        log.info("유저 등록 완료. User ID: {}, Username: {}", savedUser.getUid(), savedUser.getUsername());

        // then
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
    @DisplayName("❌ 2. 이미 존재하는 유저명으로 등록 시도 - BusinessException 발생")
    void registerUserWithDuplicateUsername() {
        log.info("---- [2] 중복 유저명 등록 테스트 시작 ----");

        UserRegisterRequest duplicateRequest = new UserRegisterRequest(
                TEST_USERNAME, "another@example.com", "anotherpass"
        );

        assertThatThrownBy(() -> userService.register(duplicateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME);

        log.warn("중복 유저명 등록 시도 -> BusinessException(DUPLICATE_USERNAME) 발생 확인.");
        log.info("---- [2] 중복 유저명 등록 테스트 종료 ----\n");
    }

    @Test
    @Order(3)
    @DisplayName("❌ 3. 이미 존재하는 이메일로 등록 시도 - BusinessException 발생")
    void registerUserWithDuplicateEmail() {
        log.info("---- [3] 중복 이메일 등록 테스트 시작 ----");

        UserRegisterRequest duplicateRequest = new UserRegisterRequest(
                "anotherUser", TEST_EMAIL, "anotherpass"
        );

        assertThatThrownBy(() -> userService.register(duplicateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        log.warn("중복 이메일 등록 시도 -> BusinessException(DUPLICATE_EMAIL) 발생 확인.");
        log.info("---- [3] 중복 이메일 등록 테스트 종료 ----\n");
    }


    @Test
    @Order(4)
    @DisplayName("✅ 4. 로그인 기능 테스트")
    void loginUserTest() {
        log.info("---- [4] 로그인 테스트 시작 ----");

        User loggedInUser = userService.login(TEST_EMAIL, TEST_PASSWORD);
        log.info("로그인 성공. User Email: {}", loggedInUser.getEmail());

        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(passwordEncoder.matches(TEST_PASSWORD, loggedInUser.getPassword())).isTrue();

        log.info("---- [4] 로그인 테스트 종료 ----\n");
    }

    @Test
    @Order(5)
    @DisplayName("❌ 5. 존재하지 않는 이메일로 로그인 시도 - BusinessException 발생")
    void loginWithNonExistentEmail() {
        log.info("---- [5] 존재하지 않는 이메일로 로그인 테스트 시작 ----");

        String nonExistentEmail = "nonexistent@example.com";
        String anyPassword = "anypass";

        assertThatThrownBy(() -> userService.login(nonExistentEmail, anyPassword))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        log.warn("존재하지 않는 이메일 로그인 시도 -> BusinessException(USER_NOT_FOUND) 발생 확인.");
        log.info("---- [5] 존재하지 않는 이메일로 로그인 테스트 종료 ----\n");
    }

    @Test
    @Order(6)
    @DisplayName("❌ 6. 잘못된 비밀번호로 로그인 시도 - BusinessException 발생")
    void loginWithWrongPassword() {
        log.info("---- [6] 잘못된 비밀번호로 로그인 테스트 시작 ----");

        assertThatThrownBy(() -> userService.login(TEST_EMAIL, "wrongpass"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);

        log.warn("잘못된 비밀번호 로그인 시도 -> BusinessException(INVALID_PASSWORD) 발생 확인.");
        log.info("---- [6] 잘못된 비밀번호로 로그인 테스트 종료 ----\n");
    }


    @Test
    @Order(7)
    @DisplayName("✅ 7. 회원정보 수정 테스트 (username, password 변경)")
    @Transactional
    void updateUserInfoTest() {
        log.info("---- [7] 회원정보 수정 테스트 시작 ----");

        // given
        // 수정 전 DB의 기존 사용자 정보를 이메일 기반으로 조회합니다.
        User userBeforeUpdate = userRepository.findByEmail(TEST_EMAIL)
                .orElseThrow(() -> new IllegalStateException("테스트 유저를 찾을 수 없습니다."));

        String newUsername = "updatedUserIntegration";
        String newPassword = "newIntegrationPass";

        Map<String, Object> updates = Map.of(
                "username", newUsername,
                "password", newPassword
        );
        log.info("수정 요청값: username={}, password={}", newUsername, newPassword);

        // when
        User updatedUser = userService.updateUserInfoByMap(userBeforeUpdate.getEmail(), updates);
        log.info("회원정보 수정 완료. 업데이트된 사용자명: {}", updatedUser.getUsername());

        // then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo(newUsername); // 새 사용자명 검증
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue(); // 새 비밀번호 암호화 후 매칭 여부 검증
        assertThat(updatedUser.getEmail()).isEqualTo(TEST_EMAIL); // 이메일은 변경되지 않았는지 확인

        User foundUpdatedUserInDb = userRepository.findByEmail(TEST_EMAIL)
                .orElseThrow(() -> new IllegalStateException("수정된 유저를 DB에서 찾을 수 없습니다 (이메일: " + TEST_EMAIL + ")."));

        assertThat(foundUpdatedUserInDb.getUsername()).isEqualTo(newUsername); // DB에서 조회한 새 사용자명 검증
        assertThat(passwordEncoder.matches(newPassword, foundUpdatedUserInDb.getPassword())).isTrue(); // DB에서 조회한 새 비밀번호 매칭 여부 검증
        assertThat(foundUpdatedUserInDb.getEmail()).isEqualTo(TEST_EMAIL); // DB에서도 이메일이 동일한지 확인
        assertThat(foundUpdatedUserInDb.getUid()).isEqualTo(updatedUser.getUid()); // UID는 동일한지 확인 (PK는 변경되지 않음)

        log.info("회원정보 수정 결과 DB 검증 완료 (이메일 기반 재조회).");
        log.info("---- [7] 회원정보 수정 테스트 종료 ----\n");
    }

    @Test
    @Order(8)
    @DisplayName("❌ 8. 이미 존재하는 username으로 수정 시도 - BusinessException 발생")
    @Transactional
    void updateUserInfoWithDuplicateUsername() {
        log.info("---- [8] 중복 username으로 수정 시도 테스트 시작 ----");

        String dummyUsername = "dummyUserForDuplication";
        String dummyEmail = "dummy@example.com";
        userRepository.save(new User(dummyUsername, dummyEmail, passwordEncoder.encode("dummyPass")));
        log.info("더미 유저 ({}) 생성 완료.", dummyUsername);

        User currentUser = userRepository.findByEmail(TEST_EMAIL)
                .orElseThrow(() -> new IllegalStateException("테스트 유저를 찾을 수 없습니다."));

        Map<String, Object> updates = Map.of("username", dummyUsername);
        log.info("수정 요청값: username={} (이미 존재하는 이름)", dummyUsername);

        assertThatThrownBy(() -> userService.updateUserInfoByMap(currentUser.getEmail(), updates))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME);

        log.warn("중복 username으로 수정 시도 -> BusinessException(DUPLICATE_USERNAME) 발생 확인.");

        userRepository.findByEmail(dummyEmail).ifPresent(userRepository::delete);
        log.info("더미 유저 ({}) 삭제 완료.", dummyUsername);

        log.info("---- [8] 중복 username으로 수정 시도 테스트 종료 ----\n");
    }


    @Test
    @Order(9)
    @DisplayName("❌ 9. 이메일 수정 시도 - BusinessException 발생")
    void updateUserInfoEmailForbidden() {
        log.info("---- [9] 이메일 수정 시도 테스트 시작 ----");

        User user = userRepository.findByEmail(TEST_EMAIL)
                .orElseThrow(() -> new IllegalStateException("테스트 유저를 찾을 수 없습니다."));
        Map<String, Object> updates = Map.of("email", "newemail@example.com");
        log.info("수정 요청값: email={}", "newemail@example.com");

        assertThatThrownBy(() -> userService.updateUserInfoByMap(user.getEmail(), updates))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_UPDATE_FORBIDDEN);

        log.warn("이메일 수정 시도 -> BusinessException(EMAIL_UPDATE_FORBIDDEN) 발생 확인.");
        log.info("---- [9] 이메일 수정 시도 테스트 종료 ----\n");
    }

    @Test
    @Order(10)
    @DisplayName("❌ 10. 허용되지 않은 필드 수정 시도 - BusinessException 발생")
    void updateUserInfoInvalidField() {
        log.info("---- [10] 허용되지 않은 필드 수정 시도 테스트 시작 ----");

        User user = userRepository.findByEmail(TEST_EMAIL)
                .orElseThrow(() -> new IllegalStateException("테스트 유저를 찾을 수 없습니다."));
        Map<String, Object> updates = Map.of("role", "admin");
        log.info("수정 요청값: role={} (허용되지 않는 필드)", "admin");

        assertThatThrownBy(() -> userService.updateUserInfoByMap(user.getEmail(), updates))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_UPDATE_FIELD);

        log.warn("허용되지 않은 필드 수정 시도 -> BusinessException(INVALID_UPDATE_FIELD) 발생 확인.");
        log.info("---- [10] 허용되지 않은 필드 수정 시도 테스트 종료 ----\n");
    }

    @Test
    @Order(11)
    @DisplayName("❌ 11. 존재하지 않는 사용자의 정보 수정 시도 - BusinessException 발생")
    void updateUserInfoUserNotFound() {
        log.info("---- [11] 존재하지 않는 사용자 정보 수정 테스트 시작 ----");

        String nonExistentEmail = "nonexistentUpdate@example.com";
        Map<String, Object> updates = Map.of("username", "newname");
        log.info("수정 시도 이메일: {} (존재하지 않음)", nonExistentEmail);

        assertThatThrownBy(() -> userService.updateUserInfoByMap(nonExistentEmail, updates))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UPDATE_NOT_FOUND);

        log.warn("존재하지 않는 사용자 정보 수정 시도 -> BusinessException(USER_UPDATE_NOT_FOUND) 발생 확인.");
        log.info("---- [11] 존재하지 않는 사용자 정보 수정 테스트 종료 ----\n");
    }


    @Test
    @Order(12)
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