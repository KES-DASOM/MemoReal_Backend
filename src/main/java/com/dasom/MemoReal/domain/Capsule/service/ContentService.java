package com.dasom.MemoReal.domain.Capsule.service;

import com.dasom.MemoReal.domain.Capsule.dto.ContentUploadRequest;
import com.dasom.MemoReal.domain.Capsule.dto.MetadataDto;
import com.dasom.MemoReal.domain.Capsule.entity.Metadata;
import com.dasom.MemoReal.domain.Capsule.repository.MetadataRepository;
import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import com.dasom.MemoReal.global.ipfs.IpfsClient;
import com.dasom.MemoReal.global.ipfs.dto.IpfsUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final MetadataRepository repository;
    private final IpfsClient IpfsClient;

    public MetadataDto upload(MultipartFile file, ContentUploadRequest request, Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.USER_ID_NOT_FOUND);
        }
        File tempFile = null;
        try {
            // MultipartFile을 임시 파일로 저장
            tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile.toPath());

            // IPFS에 업로드
            IpfsUploadResult ipfsResult = IpfsClient.upload(tempFile);

            // Metadata 엔티티 생성 및 저장
            Metadata metadata = request.toEntity(
                    ipfsResult.getFileName(),
                    file.getContentType(),
                    ipfsResult.getHash(),
                    LocalDate.now(),
                    userId
            );
            repository.save(metadata);

            // 저장된 메타데이터를 DTO로 변환 후 반환
            return MetadataDto.fromEntity(metadata);

        } catch (IOException e) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "파일 처리 중 오류 발생: " + e.getMessage());

        } finally {
            // 업로드 후 임시 파일 삭제
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.err.println("임시 파일 삭제 실패: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    // 메타데이터 조회
    public MetadataDto retrieveMetadata(Long id) {
        Metadata metadata = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.METADATA_NOT_FOUND, "메타데이터를 찾을 수 없습니다. ID: " + id));

        return MetadataDto.fromEntity(metadata);
    }

    // IPFS에서 실제 파일 다운로드
    public byte[] downloadFile(Long id) {
        Metadata metadata = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.METADATA_NOT_FOUND, "메타데이터를 찾을 수 없습니다. ID: " + id));

        MetadataDto dto = MetadataDto.fromEntity(metadata);

        LocalDate accessDate = LocalDate.parse(dto.getAccessCondition());
        if (LocalDate.now().isBefore(accessDate)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // IPFS 해시를 이용해 실제 파일 다운로드
        return IpfsClient.download(metadata.getIpfsContentHash());
    }

    public List<MetadataDto> findAllByUserId(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.USER_ID_NOT_FOUND);
        }
        List<Metadata> metadataList = repository.findAllByUserId(userId);

        if (metadataList == null) {
            throw new CustomException(ErrorCode.METADATA_NOT_FOUND,
                    "해당 유저의 메타데이터를 조회할 수 없습니다. userId: " + userId);
        }
        return metadataList.stream()
                .map(MetadataDto::fromEntity)
                .collect(Collectors.toList());
    }
    public String updateMetadataFields(Long id, Map<String, Object> updates) {
        Metadata metadata = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.METADATA_NOT_FOUND, "메타데이터를 찾을 수 없습니다. ID: " + id));

        // 수정 가능한 필드 목록
        List<String> allowedFields = List.of("filename", "contentType", "title", "description", "category", "tags");

        // 수정 불가능한 필드를 담을 리스트
        List<String> ignoredFields = new ArrayList<>();

        // 실제 수정할 필드들 처리
        updates.forEach((key, value) -> {
            if (allowedFields.contains(key)) {
                switch (key) {
                    case "filename":
                        metadata.setFilename((String) value);
                        break;
                    case "contentType":
                        metadata.setContentType((String) value);
                        break;
                    case "title":
                        metadata.setTitle((String) value);
                        break;
                    case "description":
                        metadata.setDescription((String) value);
                        break;
                    case "category":
                        metadata.setCategory((String) value);
                        break;
                    case "tags":
                        metadata.setTags((String) value);
                        break;
                }
            } else {
                ignoredFields.add(key);
            }
        });

        repository.save(metadata);

        if (ignoredFields.isEmpty()) {
            return "수정 완료.";
        } else {
            return "수정 완료. 무시된 필드: " + String.join(", ", ignoredFields);
        }
    }
}
