package com.rc.readcompass.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank
        @Size(min = 2, max = 20)
        String nickname
) {}
