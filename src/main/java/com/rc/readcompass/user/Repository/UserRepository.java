package com.rc.readcompass.user.Repository;

import com.rc.readcompass.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    // 로그인 - 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 회원가입 - 중복 체크 (논리 삭제된 사용자는 제외 → 탈퇴 유저의 값 재사용 허용)
    boolean existsByEmailAndDeletedFalse(String email);
    boolean existsByNicknameAndDeletedFalse(String nickname);

    Optional<User> findByIdAndDeletedFalse(UUID id); //닉네임 수정
}
