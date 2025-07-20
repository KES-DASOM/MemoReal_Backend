package com.dasom.MemoReal.global.ipfs;

import com.dasom.MemoReal.global.exception.CustomException;
import com.dasom.MemoReal.global.exception.ErrorCode;
import com.dasom.MemoReal.global.ipfs.dto.IpfsUploadResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class IpfsClient {

    @Value("${ipfs.api-base-url}")
    private String ipfsApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public IpfsUploadResult upload(byte[] content, String filename) {
        try {
            String url = ipfsApiBaseUrl + "/add?pin=false"; // 테스트 단계에서는 비활성화(추후 운용단계 에서는 핀설정하게 변경)

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileAsResource = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileAsResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String responseBody = response.getBody();
                String hash = extractHashFromResponse(responseBody);
                return new IpfsUploadResult(hash, filename, content.length);
            } else {
                throw new CustomException(ErrorCode.UPLOAD_FAILED, "IPFS 업로드 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "IPFS 업로드 중 예외 발생: " + e.getMessage());
        }
    }

    private String extractHashFromResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            if (root.has("Hash")) {
                return root.get("Hash").asText();
            }
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED, "응답 JSON 파싱 실패: " + e.getMessage());
        }
        throw new CustomException(ErrorCode.UPLOAD_FAILED, "응답에서 해시를 추출할 수 없습니다.");
    }

    public byte[] downloadByHash(String hash) {
        try {
            String url = ipfsApiBaseUrl + "/cat?arg=" + hash;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, request, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new CustomException(ErrorCode.CONTENT_DOWNLOAD_FAILED, "IPFS 다운로드 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CONTENT_DOWNLOAD_FAILED, "IPFS 해시 다운로드 예외: " + e.getMessage());
        }
    }

    public void deleteByHash(String hash) {
        try {
            String url = ipfsApiBaseUrl + "/pin/rm?arg=" + hash;

            restTemplate.postForEntity(url, null, String.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody.contains("not pinned")) {
                // 이미 핀되어 있지 않으면 무시
                return;
            }
            throw new CustomException(ErrorCode.CONTENT_DELETE_FAILED, "IPFS 해시 삭제 예외: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CONTENT_DELETE_FAILED, "IPFS 해시 삭제 예외: " + e.getMessage());
        }
    }
}
