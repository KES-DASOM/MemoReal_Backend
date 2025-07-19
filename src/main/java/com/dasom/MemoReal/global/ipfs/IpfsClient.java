package com.dasom.MemoReal.global.ipfs;

import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import com.dasom.MemoReal.global.ipfs.dto.IpfsUploadResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Component
public class IpfsClient {

    @Value("${ipfs.api-base-url}")
    private String ipfsApiBaseUrl;

    private final String MFS_BASE_PATH = "/test"; // MFS 폴더 경로

    // RestTemplate 재사용용 필드
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * MFS의 /test 폴더 아래에 파일 업로드
     */
    public IpfsUploadResult uploadToMfs(File file) {
        String mfsPath = MFS_BASE_PATH + "/" + file.getName();
        String url = ipfsApiBaseUrl + "/files/write?arg=" + mfsPath + "&create=true&truncate=true";

        // multipart/form-data 구성
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // Resource로 감싸서 전송
        body.add("file", new FileSystemResource(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> writeResponse = restTemplate.postForEntity(url, requestEntity, String.class);

        if (!writeResponse.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "MFS 파일 쓰기 실패: " + writeResponse.getStatusCode());
        }

        // 파일 상태 조회는 기존 코드 유지
        String statUrl = ipfsApiBaseUrl + "/files/stat?arg=" + mfsPath;
        ResponseEntity<String> statResponse = restTemplate.postForEntity(statUrl, null, String.class);
        if (!statResponse.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "MFS 파일 상태 조회 실패: " + statResponse.getStatusCode());
        }

        String bodyStr = statResponse.getBody();
        if (bodyStr == null) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "MFS 파일 상태 응답 없음");
        }

        String hash = extractHashFromJson(bodyStr);

        return new IpfsUploadResult(hash, file.getName(), file.length());

    }

    /**
     * MFS 내 /test 폴더에 저장된 파일 다운로드
     */
    public byte[] downloadFromMfs(String filename) {
        try {
            if (filename == null || filename.trim().isEmpty() || filename.endsWith("/")) {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND, "유효하지 않은 파일명입니다: " + filename);
            }

            if (!filename.startsWith("/")) {
                filename = "/" + filename;
            }

            String targetPath = MFS_BASE_PATH + filename; // "/test/filename"

            String url = ipfsApiBaseUrl + "/files/read?arg=" + targetPath;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>("", headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,  // GET이 아닌 POST로 변경해야 함
                    entity,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND, "MFS 파일 다운로드 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, "MFS 요청 실패: " + e.getMessage());
        }
    }



    /**
     * MFS 내 /test 폴더에 저장된 파일 삭제
     */
    public void deleteFromMfs(String filename) {
        try {
            String targetPath = MFS_BASE_PATH + "/" + filename;
            String url = ipfsApiBaseUrl + "/files/rm?arg=" + targetPath;

            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new CustomException(ErrorCode.CONTENT_DELETE_FAILED, "MFS 파일 삭제 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CONTENT_DELETE_FAILED, "MFS 삭제 실패: " + e.getMessage());
        }
    }

    /**
     * 간단한 JSON 문자열에서 "Hash" 키의 값을 추출하는 메서드
     */
    private String extractHashFromJson(String json) {
        // "Hash":"Qm..." 형태에서 값만 추출
        int start = json.indexOf("\"Hash\":\"") + 7; // "Hash":" 길이 = 7
        int end = json.indexOf("\"", start);
        if (start < 7 || end < 0) return "";
        return json.substring(start, end);
    }
}
