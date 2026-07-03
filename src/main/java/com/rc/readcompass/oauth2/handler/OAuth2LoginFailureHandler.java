package com.rc.readcompass.oauth2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * OAuth2 로그인 실패 시 프론트의 에러 페이지로 리다이렉트한다.
 */
@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Value("${app.frontend.auth-failure-url}")
  private String frontendAuthFailureUri;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    log.warn("OAuth2 로그인 실패: {}", exception.getMessage());
    getRedirectStrategy().sendRedirect(request, response, frontendAuthFailureUri);
  }
}
 