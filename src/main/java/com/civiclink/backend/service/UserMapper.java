package com.civiclink.backend.service;

import com.civiclink.backend.dto.UserDto;
import com.civiclink.backend.entity.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getBudgetTokens(),
                user.getProjectTokens()
        );
    }
}
