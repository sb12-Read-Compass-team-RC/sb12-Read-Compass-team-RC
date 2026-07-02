package com.rc.readcompass.user.Repository;

import com.rc.readcompass.user.entity.UserRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRankingRepository
        extends JpaRepository<UserRanking, UUID>, UserRankingQueryRepository {
}
