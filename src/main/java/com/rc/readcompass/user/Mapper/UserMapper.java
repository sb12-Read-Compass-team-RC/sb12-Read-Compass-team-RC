package com.rc.readcompass.user.Mapper;

import com.rc.readcompass.user.User;
import com.rc.readcompass.user.UserRanking;
import com.rc.readcompass.user.dto.CursorPageResponsePowerUserDto;
import com.rc.readcompass.user.dto.PowerUserDto;
import com.rc.readcompass.user.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.from(user);
    }
}