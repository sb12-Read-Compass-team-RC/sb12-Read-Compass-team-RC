package com.rc.readcompass.user.Mapper;

import com.rc.readcompass.user.entity.User;
import com.rc.readcompass.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.from(user);
    }
}
