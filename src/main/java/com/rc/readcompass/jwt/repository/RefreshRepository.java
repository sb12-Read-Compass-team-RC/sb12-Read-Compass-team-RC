package com.rc.readcompass.jwt.repository;

import com.rc.readcompass.jwt.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends JpaRepository<RefreshToken, UUID> {

  boolean existsByToken(String token);

  Optional<RefreshToken> findByToken(String token);

  void deleteByUserId(UUID userId);

  @Transactional
  void deleteByToken(String token);
}