package com.rc.readcompass.common;

import com.rc.readcompass.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContext 기반 권한 확인 유틸.
 * 역할(Role)은 요청 헤더가 아니라 검증된 JWT에서 채워진 SecurityContext를 기준으로 판단한다.
 * (헤더로는 관리자 권한을 위조할 수 없음)
 */
public final class SecurityUtils {

  private static final String ADMIN_AUTHORITY = UserRole.ADMIN.authority(); // ROLE_ADMIN

  private SecurityUtils() {
  }

  public static boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    for (GrantedAuthority authority : authentication.getAuthorities()) {
      if (ADMIN_AUTHORITY.equals(authority.getAuthority())) {
        return true;
      }
    }
    return false;
  }
}
