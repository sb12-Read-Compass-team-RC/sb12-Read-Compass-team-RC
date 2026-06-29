package com.rc.readcompass.user.Repository;

import java.util.Optional;
import java.util.UUID;

import com.rc.readcompass.user.Entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserRole.User, UUID> {

    // 로그인 - 이메일로 사용자 조회
    Optional<UserRole.User> findByEmail(String email);

    // 회원가입 - 중복 체크
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
