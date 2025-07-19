package com.dasom.MemoReal;

import com.dasom.MemoReal.domain.Capsule.dto.ContentUploadRequest;
import com.dasom.MemoReal.domain.Capsule.dto.MetadataDto;
import com.dasom.MemoReal.domain.Capsule.service.ContentService;
import com.dasom.MemoReal.domain.user.entity.User;
import com.dasom.MemoReal.domain.user.repository.UserRepository;
import com.dasom.MemoReal.global.exception.CustomException;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ContentFullIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ContentFullIntegrationTest.class);

    @Autowired
    private ContentService contentService;

    @Autowired
    private UserRepository userRepository;

    private Long testUserId;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .username("testuser")
                .password("encodedpassword")
                .email("testuser@example.com")
                .roles(new ArrayList<>() {{
                    add("USER");
                }})
                .build();
        userRepository.save(testUser);
        testUserId = testUser.getId();

        testFile = new MockMultipartFile(
                "file",
                "testfile.txt",
                "text/plain",
                "This is a test file content.".getBytes()
        );
        logger.info("\n"); // 한 줄 띄우기 (테스트 시작 전)
    }

    private MetadataDto uploadTestFile() {
        ContentUploadRequest request = ContentUploadRequest.builder()
                .title("Title")
                .description("Desc")
                .category("Category")
                .tags("tag1,tag2")
                .accessCondition(LocalDate.now().toString())
                .build();
        return contentService.upload(testFile, request, testUserId);
    }

    @Test
    void testUpload() {
        logger.info("========== testUpload 시작 ==========");
        MetadataDto metadata = uploadTestFile();

        assertNotNull(metadata);
        assertEquals("Title", metadata.getTitle());
        logger.info("testUpload 성공 - 제목: {}", metadata.getTitle());
        logger.info("========== testUpload 종료 ==========\n");
    }

    @Test
    void testRetrieveMetadata() {
        logger.info("========== testRetrieveMetadata 시작 ==========");
        MetadataDto uploaded = uploadTestFile();

        MetadataDto retrieved = contentService.retrieveMetadata(uploaded.getId());
        assertEquals(uploaded.getTitle(), retrieved.getTitle());
        logger.info("testRetrieveMetadata 성공 - 제목: {}", retrieved.getTitle());
        logger.info("========== testRetrieveMetadata 종료 ==========\n");
    }

    @Test
    void testFindAllByUserId() {
        logger.info("========== testFindAllByUserId 시작 ==========");
        uploadTestFile();

        List<MetadataDto> list = contentService.findAllByUserId(testUserId);
        assertFalse(list.isEmpty());
        logger.info("testFindAllByUserId 성공 - 메타데이터 수: {}", list.size());
        logger.info("========== testFindAllByUserId 종료 ==========\n");
    }

    @Test
    void testDownloadFile() {
        logger.info("========== testDownloadFile 시작 ==========");
        MetadataDto uploaded = uploadTestFile();

        byte[] fileData = contentService.downloadFile(uploaded.getId(), testUserId);
        assertNotNull(fileData);
        assertTrue(fileData.length > 0);
        logger.info("testDownloadFile 성공 - 파일 크기: {} bytes", fileData.length);

        // 파일 본문 출력 (텍스트 파일일 경우)
        String content = new String(fileData);
        logger.info("다운로드된 파일 내용: {}", content);

        logger.info("========== testDownloadFile 종료 ==========\n");
    }

    @Test
    void testUpdateMetadataFields() {
        logger.info("========== testUpdateMetadataFields 시작 ==========");
        MetadataDto uploaded = uploadTestFile();

        Map<String, Object> updates = Map.of("title", "Updated Title");
        contentService.updateMetadataFields(uploaded.getId(), updates, testUserId);

        MetadataDto updated = contentService.retrieveMetadata(uploaded.getId());
        assertEquals("Updated Title", updated.getTitle());
        logger.info("testUpdateMetadataFields 성공 - 수정된 제목: {}", updated.getTitle());
        logger.info("========== testUpdateMetadataFields 종료 ==========\n");
    }

    @Test
    void testDeleteMetadataAndContent() {
        logger.info("========== testDeleteMetadataAndContent 시작 ==========");
        MetadataDto uploaded = uploadTestFile();
        Long id = uploaded.getId();

        contentService.deleteMetadataAndContent(id, testUserId);
        logger.info("testDeleteMetadataAndContent - 메타데이터 및 컨텐츠 삭제 요청 완료");

        assertThrows(CustomException.class, () -> contentService.retrieveMetadata(id));
        logger.info("testDeleteMetadataAndContent 성공 - 삭제 후 메타데이터 조회 시 예외 발생 확인");
        logger.info("========== testDeleteMetadataAndContent 종료 ==========\n");
    }
}
