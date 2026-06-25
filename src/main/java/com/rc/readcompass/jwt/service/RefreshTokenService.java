package com.rc.readcompass.jwt.service;

import com.rc.readcompass.jwt.entity.RefreshToken;
import com.rc.readcompass.jwt.repository.RefreshRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshRepository refreshRepository;

  @Transactional
  public void rotate(UUID userId, String refreshToken, Instant expiry) {
    refreshRepository.deleteByUserId(userId);
    refreshRepository.flush();
    refreshRepository.save(RefreshToken.create(userId, refreshToken, expiry));
  }
}
