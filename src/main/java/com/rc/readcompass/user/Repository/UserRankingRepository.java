package com.rc.readcompass.user.Repository;

import com.rc.readcompass.user.UserRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRankingRepository extends JpaRepository<UserRanking, UUID> {
    // 파워유저 목록 조회는 Service에서 커서 페이지네이션 처리
}