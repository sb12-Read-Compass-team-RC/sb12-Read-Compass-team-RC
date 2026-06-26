package com.rc.readcompass.jwt.entity;

import com.rc.readcompass.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(name = "tb_refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 512, unique = true)
  private String token;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean isRevoked = false;

  // =====================================================
  // 도메인 메서드
  // =====================================================
  public static RefreshToken create(UUID userId, String token, Instant expiresAt) {
    return RefreshToken.builder()
        .userId(userId)
        .token(token)
        .expiresAt(expiresAt)
        .build();
  }

  @Builder
  private RefreshToken(UUID userId, String token, Instant expiresAt) {
    this.userId = userId;
    this.token = token;
    this.expiresAt = expiresAt;
    this.isRevoked = false;
  }

  public void revoke() {
    this.isRevoked = true;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(this.expiresAt);
  }
}
