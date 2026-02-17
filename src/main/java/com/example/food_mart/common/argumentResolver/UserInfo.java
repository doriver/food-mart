package com.example.food_mart.common.argumentResolver;

import com.example.food_mart.modules.user.domain.UserRole;
import lombok.Getter;

@Getter
public class UserInfo {
    private Long userId;
    private UserRole userRole;

    protected UserInfo(Long userId, UserRole userRole) {
        this.userId = userId;
        this.userRole = userRole;
    }
}
