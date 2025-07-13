package com.dasom.MemoReal.UserTest;

import com.dasom.MemoReal.UserManager.domain.dto.UserRegisterRequest;
import com.dasom.MemoReal.UserManager.domain.entity.User;
import com.dasom.MemoReal.UserManager.domain.repository.UserRepository;
import com.dasom.MemoReal.UserManager.domain.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "testpass";

    @Test
    @Order(1)
    @DisplayName("✅ 실제 DB에 유저 등록 테스트")
    void registerUserToRealDB() {
        System.out.println("---- [1] 유저 등록 테스트 시작 ----");

        // given
        UserRegisterRequest request = new UserRegisterRequest(
                TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD
        );

        // when
        User savedUser = userService.register(request);

        // then
        User foundUser = userRepository.findByUsername(TEST_USERNAME).orElseThrow();

        System.out.println("서비스 반환 User 객체:");
        System.out.println(" - ID: " + savedUser.getUid());
        System.out.println(" - Username: " + savedUser.getUsername());
        System.out.println(" - Email: " + savedUser.getEmail());
        System.out.println(" - Password (암호화됨): " + savedUser.getPassword());
        System.out.println(" - CreatedAt: " + savedUser.getCreatedAt());

        System.out.println("DB에서 조회한 User 객체:");
        System.out.println(" - ID: " + foundUser.getUid());
        System.out.println(" - Username: " + foundUser.getUsername());
        System.out.println(" - Email: " + foundUser.getEmail());
        System.out.println(" - Password (암호화됨): " + foundUser.getPassword());
        System.out.println(" - CreatedAt: " + foundUser.getCreatedAt());


        System.out.println("---- [1] 유저 등록 테스트 종료 ----\n");
    }

    @Test
    @Order(2)
    @DisplayName("✅ 로그인 기능 테스트")
    void loginUserTest() {
        System.out.println("---- [2] 로그인 테스트 시작 ----");

        // given
        String email = TEST_EMAIL;
        String password = TEST_PASSWORD;

        // when
        User loggedInUser = userService.login(email, password);

        System.out.println("로그인 후 반환된 User 객체:");
        if (loggedInUser != null) {
            System.out.println(" - ID: " + loggedInUser.getUid());
            System.out.println(" - Username: " + loggedInUser.getUsername());
            System.out.println(" - Email: " + loggedInUser.getEmail());
            System.out.println(" - Password (암호화됨): " + loggedInUser.getPassword());
            System.out.println(" - CreatedAt: " + loggedInUser.getCreatedAt());
        } else {
            System.out.println(" - 로그인 실패: User 객체가 null");
        }

        // then
        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getEmail()).isEqualTo(email);
        // 평문 비밀번호와 암호화된 비밀번호가 매칭되는지 확인
        assertThat(passwordEncoder.matches(password, loggedInUser.getPassword())).isTrue();

        System.out.println("---- [2] 로그인 테스트 종료 ----\n");
    }

    @Test
    @Order(3)
    @DisplayName("🧹 테스트 유저 삭제")
    void cleanupTestUser() {
        System.out.println("---- [3] 테스트 유저 삭제 시작 ----");

        userRepository.findByUsername(TEST_USERNAME).ifPresent(user -> {
            userRepository.delete(user);
            System.out.println("🗑️ 삭제 완료 - username: " + user.getUsername());
        });

        boolean exists = userRepository.existsByUsername(TEST_USERNAME);
        System.out.println("삭제 후 사용자 존재 여부: " + exists);

        assertThat(exists).isFalse();

        System.out.println("---- [3] 테스트 유저 삭제 종료 ----\n");
    }
}
