package com.rc.readcompass.jwt.service;

import com.rc.readcompass.jwt.TokenType;
import com.rc.readcompass.jwt.util.CookieUtil;
import com.rc.readcompass.jwt.util.JWTUtil;
import com.rc.readcompass.jwt.util.JwtHeaders;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 인증 성공 시 토큰 발급을 한 곳에서 처리한다.
 * 일반 로그인(LoginFilter), OAuth2 로그인(OAuth2LoginSuccessHandler), 재발급(ReissueController)이
 * 모두 이 서비스를 사용하므로 발급 정책(만료, Rotation, 쿠키/헤더 규약)이 항상 일치한다.
 *
 * 전달 규약:
 *  - access  : Authorization: Bearer <access> 헤더 (+ 기존 프론트 호환용 legacy 헤더)
 *  - refresh : HttpOnly 쿠키 (CookieUtil이 Secure/SameSite 일관 적용)
 */
@Service
public class TokenIssueService {

  private final JWTUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;
  private final long accessExpireMs;
  private final long refreshExpireMs;

  public TokenIssueService(JWTUtil jwtUtil,
      RefreshTokenService refreshTokenService,
      CookieUtil cookieUtil,
      @Value("${app.jwt.access-expire-ms}") long accessExpireMs,
      @Value("${app.jwt.refresh-expire-ms}") long refreshExpireMs) {
    this.jwtUtil = jwtUtil;
    this.refreshTokenService = refreshTokenService;
    this.cookieUtil = cookieUtil;
    this.accessExpireMs = accessExpireMs;
    this.refreshExpireMs = refreshExpireMs;
  }

  /**
   * access + refresh 를 모두 발급한다.
   * access 는 응답 헤더로, refresh 는 Rotation 저장 후 HttpOnly 쿠키로 내려간다.
   * 일반 로그인 성공, 재발급에서 사용.
   */
  public void issueFull(HttpServletResponse response, UUID userId, String username, String role) {
    String access = jwtUtil.createJwt(TokenType.ACCESS.category(), userId, username, role, accessExpireMs);

    issueRefresh(response, userId, username, role);

    response.setHeader("Authorization", "Bearer " + access);
    response.setHeader(JwtHeaders.LEGACY_ACCESS_HEADER, access);
  }

  /**
   * refresh 만 발급한다(Rotation 저장 + HttpOnly 쿠키).
   * OAuth2 로그인 성공 시 사용 — access 는 URL 에 노출하지 않고,
   * 프론트가 리다이렉트 도착 후 POST /api/users/reissue 로 헤더로 수령한다.
   */
  public void issueRefresh(HttpServletResponse response, UUID userId, String username, String role) {
    String refresh = jwtUtil.createJwt(TokenType.REFRESH.category(), userId, username, role, refreshExpireMs);

    Instant expiry = Instant.now().plusMillis(refreshExpireMs);
    refreshTokenService.rotate(userId, refresh, expiry);

    response.addCookie(cookieUtil.createRefresh(refresh));
  }
}
