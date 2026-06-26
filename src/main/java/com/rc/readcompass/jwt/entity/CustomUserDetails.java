package com.rc.readcompass.jwt.entity;

import com.rc.readcompass.common.Define;
import com.rc.readcompass.jwt.dto.AuthDto;
import com.rc.readcompass.user.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

  private final AuthDto authDto;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // NPE 오류 방지.
    UserRole role = authDto.getUserRole();
    if (role == null)
      role = UserRole.USER;
    return List.of(new SimpleGrantedAuthority(Define.role + role.name()));
  }

  public UUID getUserId() {
    return authDto.getId();
  }

  @Override
  public String getPassword() {
    return authDto.getPassword();
  }

  @Override
  public String getUsername() {
    return authDto.getUsername();
  }
}