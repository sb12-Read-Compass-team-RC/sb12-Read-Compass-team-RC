package com.rc.readcompass.jwt;

/**
 * JWT의 category 클레임 값이자, Refresh 토큰 쿠키 이름으로도 사용된다.
 */
public enum TokenType {
  ACCESS,
  REFRESH;

  public String category() {
    return name().toLowerCase();
  }
}
