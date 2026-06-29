package com.rc.readcompass.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//로그인
public record UserLoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {}