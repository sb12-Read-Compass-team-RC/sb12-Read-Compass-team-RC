package com.rc.readcompass.oauth2.handler;

import com.rc.readcompass.jwt.service.RefreshTokenService;
import com.rc.readcompass.jwt.util.CookieUtil;
import com.rc.readcompass.jwt.util.JWTUtil;
import com.rc.readcompass.oauth2.entity.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  // 토큰 만료 시간(ms)은 환경별 application-*.yml 에서 주입.
  @Value("${app.jwt.access-expire-ms}")
  private long accessExpireMs;

  @Value("${app.jwt.refresh-expire-ms}")
  private long refreshExpireMs;

  @Value("${app.frontend.auth-success-url}")
  private String frontendRedirectUri;

  private final JWTUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {

    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

    UUID   userId   = oAuth2User.getUserId();
    String nickname = oAuth2User.getNickname();
    String role     = "ROLE_" + oAuth2User.getRole().name();

    // 1. 토큰 생성
    String accessToken  = jwtUtil.createJwt("access",  userId, nickname, role, accessExpireMs);
    String refreshToken = jwtUtil.createJwt("refresh", userId, nickname, role, refreshExpireMs);

    // 2. Refresh Token DB 저장 (Rotation)
    Instant expiry = Instant.now().plusMillis(refreshExpireMs);
    refreshTokenService.rotate(userId, refreshToken, expiry);

    // 3. Refresh Token → HttpOnly 쿠키
    response.addCookie(cookieUtil.createRefresh(refreshToken));

    // 4. 프론트 콜백으로 리다이렉트
    String encodedNickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8);
    String redirectUrl = String.format(
        "%s?access=%s&userId=%s&nickname=%s",
        frontendRedirectUri,
        accessToken,
        userId,
        encodedNickname
    );

    log.info("OAuth2 로그인 성공: userId={}", userId);
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }
}
