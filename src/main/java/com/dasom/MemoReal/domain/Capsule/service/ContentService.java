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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final MetadataRepository repository;
    private final IpfsClient ipfsClient;

    public MetadataDto upload(MultipartFile file, ContentUploadRequest request, Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.USER_ID_NOT_FOUND);
        }
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile.toPath());

            IpfsUploadResult ipfsResult = ipfsClient.uploadToMfs(tempFile);

            Metadata metadata = request.toEntity(
                    ipfsResult.getFileName(),
                    file.getContentType(),
                    ipfsResult.getHash(),
                    LocalDate.now(),
                    userId
            );
            repository.save(metadata);

            return MetadataDto.fromEntity(metadata);

        } catch (IOException e) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "파일 처리 중 오류 발생: " + e.getMessage());

        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.err.println("임시 파일 삭제 실패: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    public MetadataDto retrieveMetadata(Long id) {
        Metadata metadata = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.METADATA_NOT_FOUND, "메타데이터를 찾을 수 없습니다. ID: " + id));

        return MetadataDto.fromEntity(metadata);
    }

    public byte[] downloadFile(Long id, Long userId) {
        Metadata metadata = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.METADATA_NOT_FOUND, "메타데이터를 찾을 수 없습니다. ID: " + id));

        if (!Objects.equals(metadata.getUserId(), userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED,"해당 메타데이터의 소유자가 아님");
        }

        MetadataDto dto = MetadataDto.fromEntity(metadata);

        LocalDate accessDate = LocalDate.parse(dto.getAccessCondition());
        if (LocalDate.now().isBefore(accessDate)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 현재는 MFS 내 저장된 파일명을 기반으로 다운로드
        // 추후 해시(IPFS content hash) 기반으로 다운로드 기능 개선 예정
        return ipfsClient.downloadFromMfs(metadata.getFilename());

        // 아래는 해시 기반 다운로드 시 사용 예시 (미구현)
        // return ipfsClient.downloadByHash(metadata.getIpfsContentHash());
    }


    public List<MetadataDto> findAllByUserId(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.USER_ID_NOT_FOUND);
        }
        List<Metadata> metadataList = repository.findAllByUserId(userId);

        if (metadataList == null || metadataList.isEmpty()) {
            throw new CustomException(ErrorCode.METADATA_NOT_FOUND,
                    "해당 유저의 메타데이터를 조회할 수 없습니다. userId: " + userId);
        }
        return metadataList.stream()
                .map(MetadataDto::fromEntity)
                .collect(Collectors.toList());
    }

    public String updateMetadataFields(Long id, Map<String, Object> updates, Long userId) {
        Metadata metadata = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.METADATA_NOT_FOUND, "메타데이터를 찾을 수 없습니다. ID: " + id));

        if (!Objects.equals(metadata.getUserId(), userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED,"메타데이터의 소유자가 아님");
        }

        List<String> allowedFields = List.of("filename", "contentType", "title", "description", "category", "tags");

        List<String> ignoredFields = new ArrayList<>();

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

    public void deleteMetadataAndContent(Long metadataId, Long userId) {
        Metadata metadata = repository.findById(metadataId)
                .orElseThrow(() -> new CustomException(ErrorCode.METADATA_NOT_FOUND));

        if (!Objects.equals(metadata.getUserId(), userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        try {
            ipfsClient.deleteFromMfs(metadata.getIpfsContentHash());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CONTENT_DELETE_FAILED, "IPFS에서 컨텐츠 삭제 실패: " + e.getMessage());
        }

        repository.delete(metadata);
    }
}
