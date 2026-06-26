package com.rc.readcompass.jwt.dto;

import com.rc.readcompass.user.Entity.UserRole;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthDto {
  private UUID id;
  private String username;
  private String password;
  private UserRole userRole;
}
