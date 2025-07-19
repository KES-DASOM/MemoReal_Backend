package com.dasom.MemoReal.domain.Capsule.controller;

import com.dasom.MemoReal.domain.Capsule.dto.ContentUploadRequest;
import com.dasom.MemoReal.domain.Capsule.dto.MetadataDto;
import com.dasom.MemoReal.domain.Capsule.service.ContentService;
import com.dasom.MemoReal.domain.user.service.UserService;
import com.dasom.MemoReal.global.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;
    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadContent(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") ContentUploadRequest metadata
    ) {
        Long userId = userService.getUserIdByUsername(SecurityUtil.getCurrentUsername());
        MetadataDto savedMetadata = contentService.upload(file, metadata, userId);
        return ResponseEntity.ok("정상적으로 업로드 되었습니다. 파일 해시: " + savedMetadata.getFileHash());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetadataDto> getMetadata(@PathVariable Long id) {
        MetadataDto dto = contentService.retrieveMetadata(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        Long userId = userService.getUserIdByUsername(SecurityUtil.getCurrentUsername());
        byte[] fileData = contentService.downloadFile(id, userId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"downloaded_file\"")
                .body(fileData);
    }

    @GetMapping("/user")
    public ResponseEntity<List<MetadataDto>> getUserContent() {
        Long userId = userService.getUserIdByUsername(SecurityUtil.getCurrentUsername());
        List<MetadataDto> list = contentService.findAllByUserId(userId);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateMetadataFields(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        Long userId = userService.getUserIdByUsername(SecurityUtil.getCurrentUsername());
        String message = contentService.updateMetadataFields(id, updates, userId);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/delete/{metadataId}")
    public ResponseEntity<String> deleteContent(@PathVariable Long metadataId) {
        Long userId = userService.getUserIdByUsername(SecurityUtil.getCurrentUsername());
        contentService.deleteMetadataAndContent(metadataId, userId);
        return ResponseEntity.ok("메타데이터 및 IPFS 컨텐츠가 성공적으로 삭제되었습니다.");
    }
}
