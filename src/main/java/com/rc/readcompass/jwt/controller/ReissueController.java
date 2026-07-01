package com.rc.readcompass.jwt.controller;

import com.rc.readcompass.jwt.TokenType;
import com.rc.readcompass.jwt.entity.RefreshToken;
import com.rc.readcompass.jwt.repository.RefreshRepository;
import com.rc.readcompass.jwt.service.RefreshTokenService;
import com.rc.readcompass.jwt.util.CookieUtil;
import com.rc.readcompass.jwt.util.JWTUtil;
import com.rc.readcompass.jwt.util.JwtHeaders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * access 토큰이 만료됐을 때 refresh 쿠키로 새 access 를 받아오는 재발급 엔드포인트.
 * 재발급 시 refresh 도 새로 발급하고 기존 것을 교체한다(Rotation).
 *
 * 주의: 이 경로는 SecurityConfig 의 permitAll + JWTFilter.shouldNotFilter 로
 *      만료된 access 헤더가 있어도 통과하도록 되어 있다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ReissueController {

  // 토큰 만료 시간(ms)은 환경별 application-*.yml 에서 주입. (@RequiredArgsConstructor 는 final 만 대상)
  @Value("${app.jwt.access-expire-ms}")
  private long accessExpireMs;

  @Value("${app.jwt.refresh-expire-ms}")
  private long refreshExpireMs;

  private final JWTUtil jwtUtil;
  private final RefreshRepository refreshRepository;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

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

    // 5. 새 토큰 발급
    UUID userId    = UUID.fromString(claims.get("userId", String.class));
    String username = claims.get("username", String.class);
    String role     = claims.get("role", String.class);

    String newAccess  = jwtUtil.createJwt(TokenType.ACCESS.category(), userId, username, role, accessExpireMs);
    String newRefresh = jwtUtil.createJwt(TokenType.REFRESH.category(), userId, username, role, refreshExpireMs);

    // 6. Rotation: 기존 refresh 제거 후 새 refresh 저장
    Instant expiry = Instant.now().plusMillis(refreshExpireMs);
    refreshTokenService.rotate(userId, newRefresh, expiry);

    // 7. 응답: Authorization: Bearer <access> (표준) + access 헤더(호환) + 새 refresh 쿠키
    response.setHeader("Authorization", "Bearer " + newAccess);
    response.setHeader(JwtHeaders.LEGACY_ACCESS_HEADER, newAccess);
    response.addCookie(cookieUtil.createRefresh(newRefresh));
    return ResponseEntity.ok(Map.of("message", "재발급 완료"));
  }

  private ResponseEntity<?> unauthorized(String message) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", message));
  }
}
