package com.rc.readcompass.jwt.util;

import com.rc.readcompass.jwt.TokenType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Refresh 토큰 쿠키 생성/삭제/조회를 한 곳에서 처리한다.
 * Secure / SameSite 속성을 환경(application-*.yml)에서 주입받아 일관되게 적용한다.
 *  - 개발(http)  : secure=false, SameSite=Lax
 *  - 운영(https) : secure=true,  SameSite=Lax (필요 시 None)
 */
@Component
public class CookieUtil {

  @Value("${app.cookie.refresh-max-age:86400}")
  private int refreshMaxAge;

  @Value("${app.cookie.secure:false}")
  private boolean secure;

  @Value("${app.cookie.same-site:Lax}")
  private String sameSite;

  /** refresh 토큰 쿠키 생성 */
  public Cookie createRefresh(String value) {
    Cookie cookie = new Cookie(TokenType.REFRESH.category(), value);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(refreshMaxAge);
    cookie.setSecure(secure);
    cookie.setAttribute("SameSite", sameSite);
    return cookie;
  }

  /** refresh 토큰 쿠키 만료(삭제) */
  public Cookie clearRefresh() {
    Cookie cookie = new Cookie(TokenType.REFRESH.category(), null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    cookie.setSecure(secure);
    cookie.setAttribute("SameSite", sameSite);
    return cookie;
  }

  /** 요청 쿠키에서 refresh 토큰을 꺼낸다. 없으면 null. (getCookies() == null 안전 처리) */
  public String readRefresh(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (TokenType.REFRESH.category().equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
