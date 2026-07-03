package com.rc.readcompass.book.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.readcompass.book.dto.NaverBookDto;
import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NaverBookClient {

  @Value("${naver.book.client-id}")
  private String clientId;

  @Value("${naver.book.client-secret}")
  private String clientSecret;

  @Value("${naver.book.base-url}")
  private String baseUrl;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public NaverBookDto searchByIsbn(String isbn) {
    String encodedIsbn = URLEncoder.encode(isbn, StandardCharsets.UTF_8);
    String apiUrl = baseUrl + "/v1/search/book.json?query=" + encodedIsbn;

    Map<String, String> requestHeaders = new HashMap<>();
    requestHeaders.put("X-Naver-Client-Id", clientId);
    requestHeaders.put("X-Naver-Client-Secret", clientSecret);

    String responseBody = get(apiUrl, requestHeaders);

    return parse(responseBody);
  }

  private String get(String apiUrl, Map<String, String> requestHeaders) {
    HttpURLConnection con = connect(apiUrl);

    try {
      con.setRequestMethod("GET");

      for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
        con.setRequestProperty(header.getKey(), header.getValue());
      }

      int responseCode = con.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        return readBody(con.getInputStream());
      }

      String errorBody = readBody(con.getErrorStream());

      throw new CustomException(ErrorCode.INVALID_REQUEST)
          .addDetail("네이버 도서 API 호출 실패: " + errorBody);
    } catch (IOException e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
          .addDetail("네이버 도서 API 요청 실패");
    } finally {
      con.disconnect();
    }
  }

  private HttpURLConnection connect(String apiUrl) {
    try {
      return (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
    } catch (IOException | IllegalArgumentException e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
          .addDetail("네이버 도서 API 연결 실패:  " + apiUrl);
    }
  }

  private String readBody(InputStream body) {
    if (body == null) {
      return "";
    }

    try (BufferedReader lineReader = new BufferedReader(
        new InputStreamReader(body, StandardCharsets.UTF_8)
    )) {
      StringBuilder responseBody = new StringBuilder();

      String line;
      while ((line = lineReader.readLine()) != null) {
        responseBody.append(line);
      }

      return responseBody.toString();
    } catch (IOException e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
          .addDetail("네이버 도서 API 응답 읽기 실패");
    }
  }

  private NaverBookDto parse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode items = root.path("items");

      if (!items.isArray() || items.isEmpty()) {
        throw new CustomException(ErrorCode.BOOK_NOT_FOUND)
            .addDetail("네이버 API에서 도서 정보를 찾지 못했습니다.");
      }

      JsonNode item = items.get(0);

      return new NaverBookDto(
          cleanHtml(item.path("title").asText()),
          cleanHtml(item.path("author").asText()),
          cleanHtml(item.path("description").asText()),
          cleanHtml(item.path("publisher").asText()),
          parsePublishedDate(item.path("pubdate").asText()),
          extractIsbn(item.path("isbn").asText()),
          item.path("image").asText(null)
      );
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
          .addDetail("네이버 도서 API 응답 변환 실패");
    }
  }

  private String cleanHtml(String text) {
    return text == null ? null : text.replaceAll("<[^>]*>", "");
  }

  private LocalDate parsePublishedDate(String pubdate) {
    if (pubdate == null || pubdate.isBlank()) {
      return null;
    }

    return LocalDate.parse(pubdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

  private String extractIsbn(String isbnText) {
    if (isbnText == null || isbnText.isBlank()) {
      return null;
    }

    String [] values = isbnText.split(" ");

    if (values.length == 0) {
      return isbnText;
    }

    return values[values.length - 1];
  }
}
