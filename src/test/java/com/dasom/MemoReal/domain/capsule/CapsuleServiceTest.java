package com.dasom.MemoReal.domain.capsule;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleRequestDto;
import com.dasom.MemoReal.domain.capsule.dto.CapsuleResponseDto;
import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import com.dasom.MemoReal.domain.capsule.repository.CapsuleRepository;
import com.dasom.MemoReal.domain.capsule.service.CapsuleService;
import com.dasom.MemoReal.domain.capsule.type.CapsuleType;
import com.dasom.MemoReal.domain.user.entity.User;
import com.dasom.MemoReal.domain.user.repository.UserRepository;
import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import com.dasom.MemoReal.global.security.util.SecurityUtil; // SecurityUtil 임포트 필요!
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic; // MockedStatic 임포트
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong; // anyLong()을 위해 추가
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class CapsuleServiceTest {

    @Mock
    private CapsuleRepository capsuleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CapsuleService capsuleService;

    private User testUser;
    private Capsule testCapsule;
    private CapsuleRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("password")
                .build();

        testCapsule = Capsule.builder()
                .id(1L)
                .title("테스트 캡슐")
                .type(CapsuleType.NORMAL)
                .content("테스트 내용")
                .openDate(LocalDate.now().plusDays(7))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .medias(new ArrayList<>())
                .build();

        testRequestDto = CapsuleRequestDto.builder()
                .title("새 캡슐 제목")
                .type(CapsuleType.NORMAL)
                .content("새 캡슐 내용")
                .openDate(LocalDate.now().plusMonths(1))
                .build();

        // 일반적으로 userRepository는 testUser를 반환하도록 설정
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("캡슐 생성 성공")
    void createCapsule_Success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            ArgumentCaptor<Capsule> capsuleCaptor = ArgumentCaptor.forClass(Capsule.class);

            when(capsuleRepository.save(capsuleCaptor.capture())).thenAnswer(invocation -> {
                Capsule capturedCapsule = invocation.getArgument(0);
                return Capsule.builder()
                        .id(1L)
                        .title(capturedCapsule.getTitle())
                        .type(capturedCapsule.getType())
                        .content(capturedCapsule.getContent())
                        .openDate(capturedCapsule.getOpenDate())
                        .user(capturedCapsule.getUser())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .medias(new ArrayList<>())
                        .build();
            });

            CapsuleResponseDto responseDto = capsuleService.createCapsule(testRequestDto);

            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getTitle()).isEqualTo(testRequestDto.getTitle());
            assertThat(responseDto.getType()).isEqualTo(testRequestDto.getType());
            assertThat(responseDto.getContent()).isEqualTo(testRequestDto.getContent());
            assertThat(responseDto.getOpenDate()).isEqualTo(testRequestDto.getOpenDate());
            assertThat(responseDto.getUser().getEmail()).isEqualTo(testUser.getEmail());

            verify(capsuleRepository, times(1)).save(any(Capsule.class));
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("단일 캡슐 조회 성공")
    void getCapsule_Success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long capsuleId = 1L;
            when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(testCapsule));

            CapsuleResponseDto responseDto = capsuleService.getCapsule(capsuleId);

            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getTitle()).isEqualTo(testCapsule.getTitle());
            assertThat(responseDto.getUser().getEmail()).isEqualTo(testUser.getEmail());
            verify(capsuleRepository, times(1)).findById(capsuleId);
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("단일 캡슐 조회 실패 - 캡슐을 찾을 수 없음")
    void getCapsule_NotFound() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long nonExistentCapsuleId = 99L;
            when(capsuleRepository.findById(nonExistentCapsuleId)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class, () ->
                    capsuleService.getCapsule(nonExistentCapsuleId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAPSULE_NOT_FOUND);
            verify(capsuleRepository, times(1)).findById(nonExistentCapsuleId);
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("단일 캡슐 조회 실패 - 권한 없음 (다른 사용자의 캡슐)")
    void getCapsule_Unauthorized() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long capsuleId = 1L;
            User otherUser = User.builder().id(2L).email("other@example.com").username("otheruser").password("pass").build();
            Capsule otherUserCapsule = Capsule.builder()
                    .id(1L)
                    .title("다른 사용자 캡슐")
                    .type(CapsuleType.NORMAL)
                    .content("다른 사용자 내용")
                    .openDate(LocalDate.now().plusDays(10))
                    .user(otherUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .medias(new ArrayList<>())
                    .build();

            when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(otherUserCapsule));

            CustomException exception = assertThrows(CustomException.class, () ->
                    capsuleService.getCapsule(capsuleId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
            verify(capsuleRepository, times(1)).findById(capsuleId);
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("모든 캡슐 조회 성공 (현재 사용자 캡슐만)")
    void getAllCapsules_Success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Capsule anotherCapsule = Capsule.builder()
                    .id(2L)
                    .title("다른 내 캡슐")
                    .type(CapsuleType.NORMAL)
                    .content("내용2")
                    .openDate(LocalDate.now().plusDays(14))
                    .user(testUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .medias(new ArrayList<>())
                    .build();

            List<Capsule> userCapsules = Arrays.asList(testCapsule, anotherCapsule);
            when(capsuleRepository.findByUser(testUser)).thenReturn(userCapsules);

            List<CapsuleResponseDto> responseDtos = capsuleService.getAllCapsules();

            assertThat(responseDtos).isNotNull();
            assertThat(responseDtos).hasSize(2);
            assertThat(responseDtos.get(0).getTitle()).isEqualTo(testCapsule.getTitle());
            assertThat(responseDtos.get(1).getTitle()).isEqualTo(anotherCapsule.getTitle());
            responseDtos.forEach(dto -> {
                assertThat(dto.getUser().getEmail()).isEqualTo(testUser.getEmail());
            });
            verify(capsuleRepository, times(1)).findByUser(testUser);
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("캡슐 업데이트 성공")
    void updateCapsule_Success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long capsuleId = 1L;
            when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(testCapsule));
            when(capsuleRepository.save(any(Capsule.class))).thenReturn(testCapsule);

            CapsuleRequestDto updateRequestDto = CapsuleRequestDto.builder()
                    .title("업데이트된 제목")
                    .type(CapsuleType.TIME)
                    .content("업데이트된 내용")
                    .openDate(LocalDate.now().plusDays(30))
                    .build();

            CapsuleResponseDto responseDto = capsuleService.updateCapsule(capsuleId, updateRequestDto);

            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getTitle()).isEqualTo(updateRequestDto.getTitle());
            assertThat(responseDto.getType()).isEqualTo(updateRequestDto.getType());
            assertThat(responseDto.getContent()).isEqualTo(updateRequestDto.getContent());
            assertThat(responseDto.getOpenDate()).isEqualTo(updateRequestDto.getOpenDate());
            assertThat(responseDto.getUser().getEmail()).isEqualTo(testUser.getEmail());
            verify(capsuleRepository, times(1)).findById(capsuleId);
            verify(capsuleRepository, times(1)).save(any(Capsule.class));
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("캡슐 업데이트 실패 - 캡슐을 찾을 수 없음")
    void updateCapsule_NotFound() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long nonExistentCapsuleId = 99L;
            when(capsuleRepository.findById(nonExistentCapsuleId)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class, () ->
                    capsuleService.updateCapsule(nonExistentCapsuleId, testRequestDto)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAPSULE_NOT_FOUND);
            verify(capsuleRepository, times(1)).findById(nonExistentCapsuleId);
            verify(capsuleRepository, never()).save(any(Capsule.class));
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("캡슐 업데이트 실패 - 권한 없음")
    void updateCapsule_Unauthorized() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long capsuleId = 1L;
            User otherUser = User.builder().id(2L).email("other@example.com").username("otheruser").password("pass").build();
            Capsule otherUserCapsule = Capsule.builder()
                    .id(1L)
                    .title("다른 사용자 캡슐")
                    .type(CapsuleType.TIME)
                    .content("다른 사용자 내용")
                    .openDate(LocalDate.now().plusDays(10))
                    .user(otherUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .medias(new ArrayList<>())
                    .build();

            when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(otherUserCapsule));

            CustomException exception = assertThrows(CustomException.class, () ->
                    capsuleService.updateCapsule(capsuleId, testRequestDto)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
            verify(capsuleRepository, times(1)).findById(capsuleId);
            verify(capsuleRepository, never()).save(any(Capsule.class));
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("캡슐 삭제 성공")
    void deleteCapsule_Success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long capsuleId = 1L;
            when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(testCapsule));
            doNothing().when(capsuleRepository).delete(any(Capsule.class));

            capsuleService.deleteCapsule(capsuleId);

            verify(capsuleRepository, times(1)).findById(capsuleId);
            verify(capsuleRepository, times(1)).delete(testCapsule);
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("캡슐 삭제 실패 - 캡슐을 찾을 수 없음")
    void deleteCapsule_NotFound() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long nonExistentCapsuleId = 99L;
            when(capsuleRepository.findById(nonExistentCapsuleId)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class, () ->
                    capsuleService.deleteCapsule(nonExistentCapsuleId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAPSULE_NOT_FOUND);
            verify(capsuleRepository, times(1)).findById(nonExistentCapsuleId);
            verify(capsuleRepository, never()).delete(any(Capsule.class));
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    @Test
    @DisplayName("캡슐 삭제 실패 - 권한 없음")
    void deleteCapsule_Unauthorized() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn(testUser.getEmail()); // SecurityUtil 정상 동작 Mocking

            Long capsuleId = 1L;
            User otherUser = User.builder().id(2L).email("other@example.com").username("otheruser").password("pass").build();
            Capsule otherUserCapsule = Capsule.builder()
                    .id(1L)
                    .title("다른 사용자 캡슐")
                    .type(CapsuleType.TIME)
                    .content("다른 사용자 내용")
                    .openDate(LocalDate.now().plusDays(10))
                    .user(otherUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .medias(new ArrayList<>())
                    .build();

            when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(otherUserCapsule));

            CustomException exception = assertThrows(CustomException.class, () ->
                    capsuleService.deleteCapsule(capsuleId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
            verify(capsuleRepository, times(1)).findById(capsuleId);
            verify(capsuleRepository, never()).delete(any(Capsule.class));
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
        }
    }

    // --- 새로 추가되거나 수정된 테스트 ---

    @Test
    @DisplayName("현재 사용자 정보 없을 시 RuntimeException 발생 - 캡슐 생성")
    void createCapsule_SecurityUtilThrowsRuntimeException() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            // SecurityUtil.getCurrentUsername()이 RuntimeException을 던지도록 모킹
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenThrow(new RuntimeException("인증되지 않은 접근입니다.")); // 메시지 수정

            // CapsuleService는 이 RuntimeException을 그대로 전파해야 함
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    capsuleService.createCapsule(testRequestDto)
            );

            assertThat(exception.getMessage()).isEqualTo("인증되지 않은 접근입니다."); // 메시지 수정
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
            verify(userRepository, never()).findByEmail(anyString()); // 예외 발생 시 호출되면 안 됨
            verify(capsuleRepository, never()).save(any(Capsule.class)); // 예외 발생 시 호출되면 안 됨
        }
    }

    @Test
    @DisplayName("현재 사용자 정보 없을 시 RuntimeException 발생 - 캡슐 조회")
    void getCapsule_SecurityUtilThrowsRuntimeException() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenThrow(new RuntimeException("인증되지 않은 접근입니다.")); // 메시지 수정

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    capsuleService.getCapsule(1L)
            );

            assertThat(exception.getMessage()).isEqualTo("인증되지 않은 접근입니다."); // 메시지 수정
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
            verify(capsuleRepository, never()).findById(anyLong());
        }
    }

    @Test
    @DisplayName("현재 사용자 정보 없을 시 RuntimeException 발생 - 모든 캡슐 조회")
    void getAllCapsules_SecurityUtilThrowsRuntimeException() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenThrow(new RuntimeException("인증되지 않은 접근입니다.")); // 메시지 수정

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    capsuleService.getAllCapsules()
            );

            assertThat(exception.getMessage()).isEqualTo("인증되지 않은 접근입니다."); // 메시지 수정
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
            verify(userRepository, never()).findByEmail(anyString());
            verify(capsuleRepository, never()).findByUser(any(User.class));
        }
    }

    @Test
    @DisplayName("현재 사용자 정보 없을 시 RuntimeException 발생 - 캡슐 업데이트")
    void updateCapsule_SecurityUtilThrowsRuntimeException() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenThrow(new RuntimeException("인증되지 않은 접근입니다.")); // 메시지 수정

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    capsuleService.updateCapsule(1L, testRequestDto)
            );

            assertThat(exception.getMessage()).isEqualTo("인증되지 않은 접근입니다."); // 메시지 수정
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
            verify(capsuleRepository, never()).findById(anyLong());
            verify(capsuleRepository, never()).save(any(Capsule.class));
        }
    }

    @Test
    @DisplayName("현재 사용자 정보 없을 시 RuntimeException 발생 - 캡슐 삭제")
    void deleteCapsule_SecurityUtilThrowsRuntimeException() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenThrow(new RuntimeException("인증되지 않은 접근입니다.")); // 메시지 수정

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    capsuleService.deleteCapsule(1L)
            );

            assertThat(exception.getMessage()).isEqualTo("인증되지 않은 접근입니다."); // 메시지 수정
            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
            verify(capsuleRepository, never()).findById(anyLong());
            verify(capsuleRepository, never()).delete(any(Capsule.class));
        }
    }

    @Test
    @DisplayName("현재 사용자 가져오기 실패 - 사용자를 찾을 수 없음 (DB에 없음)")
    void getCurrentUser_UserNotFound() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername)
                    .thenReturn("nonexistent@example.com"); // 존재하지 않는 이메일 반환

            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class, () ->
                    capsuleService.createCapsule(testRequestDto)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);

            mockedSecurityUtil.verify(SecurityUtil::getCurrentUsername, times(1));
            verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        }
    }
}