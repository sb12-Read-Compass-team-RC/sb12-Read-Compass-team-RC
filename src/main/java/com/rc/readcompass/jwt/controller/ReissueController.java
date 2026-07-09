package com.rc.readcompass.jwt.controller;

import com.rc.readcompass.jwt.TokenType;
import com.rc.readcompass.jwt.entity.RefreshToken;
import com.rc.readcompass.jwt.repository.RefreshRepository;
import com.rc.readcompass.jwt.service.TokenIssueService;
import com.rc.readcompass.jwt.util.CookieUtil;
import com.rc.readcompass.jwt.util.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * access 토큰이 만료됐을 때 refresh 쿠키로 새 access 를 받아오는 재발급 엔드포인트.
 * 재발급 시 refresh 도 새로 발급하고 기존 것을 교체한다(Rotation) — TokenIssueService 가 처리.
 *
 * OAuth2 로그인 직후에도 프론트가 이 엔드포인트를 호출해 access 를 수령하므로,
 * 응답 바디는 일반 로그인 응답과 동일한 형태(id/nickname/role)를 반환한다.
 *
 * 주의: 이 경로는 SecurityConfig 의 permitAll + JWTFilter.shouldNotFilter 로
 *      만료된 access 헤더가 있어도 통과하도록 되어 있다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ReissueController {

  private final JWTUtil jwtUtil;
  private final RefreshRepository refreshRepository;
  private final TokenIssueService tokenIssueService;
  private final CookieUtil cookieUtil;
  private final com.rc.readcompass.user.Repository.UserRepository userRepository;

  @PostMapping("/reissue")
  public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

    // 1. 쿠키에서 refresh 추출
    String refresh = cookieUtil.readRefresh(request);
    if (refresh == null) {
      return unauthorized("refresh token이 없습니다.");
    }

    // 2. 서명/만료 검증 (한 번만 파싱)
    Claims claims;
    try {
      claims = jwtUtil.getClaims(refresh);
    } catch (ExpiredJwtException e) {
      return unauthorized("refresh token이 만료되었습니다. 다시 로그인하세요.");
    } catch (JwtException | IllegalArgumentException e) {
      return unauthorized("유효하지 않은 refresh token입니다.");
    }

    // 3. category 가 refresh 인지 확인
    if (!TokenType.REFRESH.category().equals(claims.get("category", String.class))) {
      return unauthorized("유효하지 않은 refresh token입니다.");
    }

    // 4. DB에 존재하고, revoke/만료되지 않았는지 확인
    RefreshToken stored = refreshRepository.findByToken(refresh).orElse(null);
    if (stored == null || stored.isRevoked() || stored.isExpired()) {
      return unauthorized("유효하지 않은 refresh token입니다.");
    }

    UUID userId     = UUID.fromString(claims.get("userId", String.class));
    String username = claims.get("username", String.class);
    String role     = claims.get("role", String.class);

    // 사용자가 DB에 실제로 존재하고 탈퇴하지 않았는지 확인한다.
    if (!userRepository.existsByIdAndDeletedFalse(userId)) {
      return unauthorized("유효하지 않은 사용자입니다. 다시 로그인하세요.");
    }

    // 5. 새 access/refresh 발급 + Rotation + 헤더/쿠키 전달 (LoginFilter 와 동일 정책)
    tokenIssueService.issueFull(response, userId, username, role);

    return ResponseEntity.ok(Map.of(
        "id", userId.toString(),
        "nickname", username,
        "role", role
    ));
  }

  private ResponseEntity<?> unauthorized(String message) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", message));
  }
}