package com.rc.readcompass.oauth2.entity;

import com.rc.readcompass.oauth2.dto.OAuth2Response;
import com.rc.readcompass.user.User;
import com.rc.readcompass.user.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomOAuth2User implements OAuth2User {

  private final UUID userId;
  private final String nickname;
  private final UserRole role;
  private final Map<String, Object> attributes;

  public CustomOAuth2User(User user, OAuth2Response userInfo) {
    this.userId     = user.getId();
    this.nickname   = user.getNickname();
    this.role       = user.getRole();
    this.attributes = Map.copyOf(userInfo instanceof Map<?,?> m
        ? (Map<String, Object>) m       // 이 분기는 실제로 타지 않음 - 타입 안전용
        : Map.of("email", userInfo.getEmail(), "provider", userInfo.getProvider()));
  }

  // =====================================================
  // OAuth2User
  // =====================================================

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.authority()));
  }

  @Override
  public String getName() {
    return nickname;
  }
}
