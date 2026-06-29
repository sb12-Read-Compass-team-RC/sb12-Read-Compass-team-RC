package com.rc.readcompass.user.dto;

import com.rc.readcompass.user.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String nickname,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}