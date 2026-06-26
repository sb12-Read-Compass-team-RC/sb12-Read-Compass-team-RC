package com.rc.readcompass.user.Mapper;

import com.rc.readcompass.user.Entity.UserRole.User;
import com.rc.readcompass.user.Entity.UserRanking;
import com.rc.readcompass.user.dto.PowerUserDto;
import com.rc.readcompass.user.dto.UserResponse;
import org.springframework.stereotype.Component;
import com.rc.readcompass.user.dto.CursorPageResponsePowerUserDto;

import java.time.Instant;
import java.util.List;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.from(user);
    }

    public PowerUserDto toPowerUserDto(UserRanking ranking) {
        return new PowerUserDto(
                ranking.getUserId(),
                ranking.getNickname(),
                ranking.getPeriodType().name(),
                ranking.getCreatedAt(),
                (long)ranking.getRankPosition(),
                ranking.getScore().doubleValue(),
                ranking.getReviewScoreSum().doubleValue(),
                ranking.getLikeCount(),
                ranking.getCommentCount()
        );
    }

    public CursorPageResponsePowerUserDto toCursorPageResponse(
            List<PowerUserDto> content,
            String nextCursor,
            Instant nextAfter,
            Integer size,
            Long totalElements,
            Boolean hasNext
    ) {
        return new CursorPageResponsePowerUserDto(
                content,
                nextCursor,
                nextAfter,
                size,
                totalElements,
                hasNext
        );
    }
}