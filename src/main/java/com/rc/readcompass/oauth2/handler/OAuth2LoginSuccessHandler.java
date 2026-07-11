package com.rc.readcompass.oauth2.handler;

import com.rc.readcompass.jwt.service.TokenIssueService;
import com.rc.readcompass.oauth2.entity.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

  @Value("${app.frontend.auth-success-url}")
  private String frontendRedirectUri;

  private final TokenIssueService tokenIssueService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {

    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

    UUID   userId   = oAuth2User.getUserId();
    String nickname = oAuth2User.getNickname();
    String role     = oAuth2User.getRole().authority();

    // refresh 발급(Rotation) + HttpOnly 쿠키.
    tokenIssueService.issueRefresh(response, userId, nickname, role);

    log.info("OAuth2 로그인 성공: userId={}", userId);
    getRedirectStrategy().sendRedirect(request, response, frontendRedirectUri);
  }
}