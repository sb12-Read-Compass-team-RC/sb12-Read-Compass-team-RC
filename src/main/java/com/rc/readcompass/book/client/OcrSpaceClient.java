package com.rc.readcompass.book.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
public class OcrSpaceClient {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${ocr.space.api-key}")
  private String apiKey;

  @Value("${ocr.space.url}")
  private String ocrUrl;

  public String extractText(MultipartFile image) {
    validateImage(image);

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.set("apikey", apiKey);

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("file", toResource(image));
      body.add("language", "kor");
      body.add("isOverlayRequired", "false");
      body.add("detectOrientation", "true");
      body.add("scale", "true");
      body.add("OCREngine", "2");

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response = restTemplate.postForEntity(
          ocrUrl,
          requestEntity,
          String.class
      );

      return parseText(response.getBody());

    } catch (RestClientException e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
          .addDetail("OCR Space API 호출에 실패했습니다.");
    } catch (IOException e) {
      throw new CustomException(ErrorCode.INVALID_REQUEST)
          .addDetail("OCR 이미지 파일을 읽을 수 없습니다.");
    }
  }

  private void validateImage(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_REQUEST)
          .addDetail("이미지 파일이 비어 있습니다.");
    }

    if (image.getSize() > 1024 * 1024) {
      throw new CustomException(ErrorCode.INVALID_REQUEST)
          .addDetail("OCR 무료 플랜에서는 1MB 이하 이미지만 처리할 수 있습니다.");
    }
  }

  private ByteArrayResource toResource(MultipartFile image) throws IOException {
    return new ByteArrayResource(image.getBytes()) {
      @Override
      public String getFilename() {
        return image.getOriginalFilename();
      }
    };
  }

  private String parseText(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);

      boolean isErrored = root.path("IsErroredOnProcessing").asBoolean(false);
      JsonNode parsedResults = root.path("ParsedResults");

      if (isErrored || !parsedResults.isArray() || parsedResults.isEmpty()) {
        throw new CustomException(ErrorCode.INVALID_REQUEST)
            .addDetail("OCR 인식에 실패했습니다.");
      }

      String parsedText = parsedResults.get(0).path("ParsedText").asText();

      if (parsedText == null || parsedText.isBlank()) {
        throw new CustomException(ErrorCode.INVALID_REQUEST)
            .addDetail("OCR 결과 텍스트가 비어 있습니다.");
      }

      return parsedText;

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
          .addDetail("OCR 응답을 처리할 수 없습니다.");
    }
  }
}