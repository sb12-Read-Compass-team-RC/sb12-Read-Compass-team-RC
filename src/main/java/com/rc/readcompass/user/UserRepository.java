package com.rc.readcompass.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    // 로그인 - 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 회원가입 - 중복 체크
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
