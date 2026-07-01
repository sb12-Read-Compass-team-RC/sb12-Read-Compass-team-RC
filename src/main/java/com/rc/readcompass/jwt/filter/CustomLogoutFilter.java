package com.rc.readcompass.jwt.filter;

import com.rc.readcompass.jwt.TokenType;
import com.rc.readcompass.jwt.repository.RefreshRepository;
import com.rc.readcompass.jwt.util.CookieUtil;
import com.rc.readcompass.jwt.util.JWTUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

@AllArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

  private final JWTUtil jwtUtil;
  private final RefreshRepository refreshRepository;
  private final CookieUtil cookieUtil;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

    // path and method verify
    String requestUri = request.getRequestURI();
    if (!requestUri.matches("^\\/api\\/users\\/logout$")) {
      filterChain.doFilter(request, response);
      return;
    }
    String requestMethod = request.getMethod();
    if (!requestMethod.equals("POST")) {
      filterChain.doFilter(request, response);
      return;
    }

    // get refresh token (쿠키 없음/null 안전 처리)
    String refresh = cookieUtil.readRefresh(request);
    if (refresh == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // 만료 / 위조·형식오류 검증
    try {
      jwtUtil.getClaims(refresh);
    } catch (JwtException | IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // 토큰이 refresh인지 확인
    String category = jwtUtil.getCategory(refresh);
    if (!category.equals(TokenType.REFRESH.category())) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // DB에 저장되어 있는지 확인
    boolean isExist = refreshRepository.existsByToken(refresh);
    if (!isExist) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // 로그아웃 진행 — Refresh 토큰 DB에서 제거 + 쿠키 만료
    refreshRepository.deleteByToken(refresh);
    response.addCookie(cookieUtil.clearRefresh());
    response.setStatus(HttpServletResponse.SC_OK);
  }
}