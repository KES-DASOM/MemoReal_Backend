package com.dasom.MemoReal.domain.capsule;

import com.dasom.MemoReal.domain.capsule.dto.CapsuleDto;
import com.dasom.MemoReal.domain.capsule.entity.Capsule;
import com.dasom.MemoReal.domain.capsule.repository.CapsuleRepository;
import com.dasom.MemoReal.domain.capsule.service.CapsuleService;
import com.dasom.MemoReal.domain.capsule.type.CapsuleType;
import com.dasom.MemoReal.domain.user.entity.User;
import com.dasom.MemoReal.domain.user.repository.UserRepository;
import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    private CapsuleDto.CapsuleRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        // Spring Security Context Mocking
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 테스트에 사용될 사용자(User) 객체 설정
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("password")
                .build();

        // 테스트에 사용될 캡슐(Capsule) 객체 설정 (업데이트/조회/삭제 등에서 사용)
        // 핵심 변경 사항: builder() 체인에서 id()를 호출하지 않고,
        // build() 후에 setId() 메서드를 호출하여 id를 설정합니다.
        testCapsule = Capsule.builder()
                .title("테스트 캡슐")
                .type(CapsuleType.TIME)
                .content("테스트 내용")
                .openDate(LocalDate.now().plusDays(7))
                .user(testUser) // Capsule 엔티티의 @Builder 생성자에 user가 포함되어 있으므로 빌더로 설정하는 것이 좋습니다.
                .build();
        testCapsule.setId(1L); // <-- 이 부분이 @Setter가 있어야 가능하며, 빌더 체인 밖에서 호출됩니다.

        // 테스트에 사용될 요청 DTO (create/update 등에서 사용)
        testRequestDto = CapsuleDto.CapsuleRequestDto.builder()
                .title("새 캡슐 제목")
                .type(CapsuleType.NORMAL)
                .content("새 캡슐 내용")
                .openDate(LocalDate.now().plusMonths(1))
                .build();

        // 모든 테스트에서 공통으로 필요한 Mocking 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("캡슐 생성 성공")
    void createCapsule_Success() {
        ArgumentCaptor<Capsule> capsuleCaptor = ArgumentCaptor.forClass(Capsule.class);

        when(capsuleRepository.save(capsuleCaptor.capture())).thenAnswer(invocation -> {
            Capsule capturedCapsule = invocation.getArgument(0);
            capturedCapsule.setId(1L); // 가상의 ID 설정
            return capturedCapsule;
        });

        CapsuleDto.CapsuleResponseDto responseDto = capsuleService.createCapsule(testRequestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getTitle()).isEqualTo(testRequestDto.getTitle());
        assertThat(responseDto.getType()).isEqualTo(testRequestDto.getType());
        assertThat(responseDto.getContent()).isEqualTo(testRequestDto.getContent());
        assertThat(responseDto.getOpenDate()).isEqualTo(testRequestDto.getOpenDate());
        assertThat(responseDto.getUser().getEmail()).isEqualTo(testUser.getEmail());

        Capsule savedCapsule = capsuleCaptor.getValue();
        assertThat(savedCapsule.getTitle()).isEqualTo(testRequestDto.getTitle());
        assertThat(savedCapsule.getType()).isEqualTo(testRequestDto.getType());
        assertThat(savedCapsule.getContent()).isEqualTo(testRequestDto.getContent());
        assertThat(savedCapsule.getOpenDate()).isEqualTo(testRequestDto.getOpenDate());
        assertThat(savedCapsule.getUser()).isEqualTo(testUser);

        verify(capsuleRepository, times(1)).save(any(Capsule.class));
    }

    @Test
    @DisplayName("단일 캡슐 조회 성공")
    void getCapsule_Success() {
        Long capsuleId = 1L;
        when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(testCapsule));

        CapsuleDto.CapsuleResponseDto responseDto = capsuleService.getCapsule(capsuleId);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getTitle()).isEqualTo(testCapsule.getTitle());
        assertThat(responseDto.getUser().getEmail()).isEqualTo(testUser.getEmail());
        verify(capsuleRepository, times(1)).findById(capsuleId);
    }

    @Test
    @DisplayName("단일 캡슐 조회 실패 - 캡슐을 찾을 수 없음")
    void getCapsule_NotFound() {
        Long nonExistentCapsuleId = 99L;
        when(capsuleRepository.findById(nonExistentCapsuleId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () ->
                capsuleService.getCapsule(nonExistentCapsuleId)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAPSULE_NOT_FOUND);
        verify(capsuleRepository, times(1)).findById(nonExistentCapsuleId);
    }

    @Test
    @DisplayName("단일 캡슐 조회 실패 - 권한 없음 (다른 사용자의 캡슐)")
    void getCapsule_Unauthorized() {
        Long capsuleId = 1L;
        User otherUser = User.builder().id(2L).email("other@example.com").username("otheruser").password("pass").build();
        Capsule otherUserCapsule = Capsule.builder()
                .title("다른 사용자 캡슐")
                .type(CapsuleType.NORMAL)
                .content("다른 사용자 내용")
                .openDate(LocalDate.now().plusDays(10))
                .user(otherUser) // user 필드도 빌더로 설정하는 것이 좋습니다.
                .build();
        otherUserCapsule.setId(1L); // <-- @Setter 덕분에 가능

        when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(otherUserCapsule));

        CustomException exception = assertThrows(CustomException.class, () ->
                capsuleService.getCapsule(capsuleId)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(capsuleRepository, times(1)).findById(capsuleId);
    }

    @Test
    @DisplayName("모든 캡슐 조회 성공 (현재 사용자 캡슐만)")
    void getAllCapsules_Success() {
        Capsule anotherCapsule = Capsule.builder()
                .title("다른 내 캡슐")
                .type(CapsuleType.NORMAL)
                .content("내용2")
                .openDate(LocalDate.now().plusDays(14))
                .user(testUser) // user 필드도 빌더로 설정하는 것이 좋습니다.
                .build();
        anotherCapsule.setId(2L); // <-- @Setter 덕분에 가능

        List<Capsule> userCapsules = Arrays.asList(testCapsule, anotherCapsule);
        when(capsuleRepository.findByUser(testUser)).thenReturn(userCapsules);

        List<CapsuleDto.CapsuleResponseDto> responseDtos = capsuleService.getAllCapsules();

        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos.get(0).getTitle()).isEqualTo(testCapsule.getTitle());
        assertThat(responseDtos.get(1).getTitle()).isEqualTo(anotherCapsule.getTitle());
        responseDtos.forEach(dto -> assertThat(dto.getUser().getEmail()).isEqualTo(testUser.getEmail()));
        verify(capsuleRepository, times(1)).findByUser(testUser);
    }

    @Test
    @DisplayName("캡슐 업데이트 성공")
    void updateCapsule_Success() {
        Long capsuleId = 1L;
        testCapsule.update("업데이트 전 제목", CapsuleType.TIME, "업데이트 전 내용", LocalDate.now().plusDays(5));
        when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(testCapsule));
        when(capsuleRepository.save(any(Capsule.class))).thenReturn(testCapsule);

        CapsuleDto.CapsuleRequestDto updateRequestDto = CapsuleDto.CapsuleRequestDto.builder()
                .title("업데이트된 제목")
                .type(CapsuleType.NORMAL)
                .content("업데이트된 내용")
                .openDate(LocalDate.now().plusDays(30))
                .build();

        CapsuleDto.CapsuleResponseDto responseDto = capsuleService.updateCapsule(capsuleId, updateRequestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getTitle()).isEqualTo(updateRequestDto.getTitle());
        assertThat(responseDto.getType()).isEqualTo(updateRequestDto.getType());
        assertThat(responseDto.getContent()).isEqualTo(updateRequestDto.getContent());
        assertThat(responseDto.getOpenDate()).isEqualTo(updateRequestDto.getOpenDate());
        assertThat(responseDto.getUser().getEmail()).isEqualTo(testUser.getEmail());
        verify(capsuleRepository, times(1)).findById(capsuleId);
        verify(capsuleRepository, times(1)).save(any(Capsule.class));
    }

    @Test
    @DisplayName("캡슐 업데이트 실패 - 캡슐을 찾을 수 없음")
    void updateCapsule_NotFound() {
        Long nonExistentCapsuleId = 99L;
        when(capsuleRepository.findById(nonExistentCapsuleId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () ->
                capsuleService.updateCapsule(nonExistentCapsuleId, testRequestDto)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAPSULE_NOT_FOUND);
        verify(capsuleRepository, times(1)).findById(nonExistentCapsuleId);
        verify(capsuleRepository, never()).save(any(Capsule.class));
    }

    @Test
    @DisplayName("캡슐 업데이트 실패 - 권한 없음")
    void updateCapsule_Unauthorized() {
        Long capsuleId = 1L;
        User otherUser = User.builder().id(2L).email("other@example.com").username("otheruser").password("pass").build();
        Capsule otherUserCapsule = Capsule.builder()
                .title("다른 사용자 캡슐")
                .type(CapsuleType.NORMAL)
                .content("다른 사용자 내용")
                .openDate(LocalDate.now().plusDays(10))
                .user(otherUser) // user 필드도 빌더로 설정하는 것이 좋습니다.
                .build();
        otherUserCapsule.setId(1L); // <-- @Setter 덕분에 가능

        when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(otherUserCapsule));

        CustomException exception = assertThrows(CustomException.class, () ->
                capsuleService.updateCapsule(capsuleId, testRequestDto)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(capsuleRepository, times(1)).findById(capsuleId);
        verify(capsuleRepository, never()).save(any(Capsule.class));
    }

    @Test
    @DisplayName("캡슐 삭제 성공")
    void deleteCapsule_Success() {
        Long capsuleId = 1L;
        when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(testCapsule));
        doNothing().when(capsuleRepository).delete(any(Capsule.class));

        capsuleService.deleteCapsule(capsuleId);

        verify(capsuleRepository, times(1)).findById(capsuleId);
        verify(capsuleRepository, times(1)).delete(testCapsule);
    }

    @Test
    @DisplayName("캡슐 삭제 실패 - 캡슐을 찾을 수 없음")
    void deleteCapsule_NotFound() {
        Long nonExistentCapsuleId = 99L;
        when(capsuleRepository.findById(nonExistentCapsuleId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () ->
                capsuleService.deleteCapsule(nonExistentCapsuleId)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAPSULE_NOT_FOUND);
        verify(capsuleRepository, times(1)).findById(nonExistentCapsuleId);
        verify(capsuleRepository, never()).delete(any(Capsule.class));
    }

    @Test
    @DisplayName("캡슐 삭제 실패 - 권한 없음")
    void deleteCapsule_Unauthorized() {
        Long capsuleId = 1L;
        User otherUser = User.builder().id(2L).email("other@example.com").username("otheruser").password("pass").build();
        Capsule otherUserCapsule = Capsule.builder()
                .title("다른 사용자 캡슐")
                .type(CapsuleType.NORMAL)
                .content("다른 사용자 내용")
                .openDate(LocalDate.now().plusDays(10))
                .user(otherUser) // user 필드도 빌더로 설정하는 것이 좋습니다.
                .build();
        otherUserCapsule.setId(1L); // <-- @Setter 덕분에 가능

        when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(otherUserCapsule));

        CustomException exception = assertThrows(CustomException.class, () ->
                capsuleService.deleteCapsule(capsuleId)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(capsuleRepository, times(1)).findById(capsuleId);
        verify(capsuleRepository, never()).delete(any(Capsule.class));
    }

    @Test
    @DisplayName("현재 사용자 가져오기 실패 - 인증되지 않은 접근")
    void getCurrentUser_Unauthorized() {
        when(securityContext.getAuthentication()).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () ->
                capsuleService.createCapsule(testRequestDto)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("현재 사용자 가져오기 실패 - 사용자를 찾을 수 없음")
    void getCurrentUser_UserNotFound() {
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () ->
                capsuleService.createCapsule(testRequestDto)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}