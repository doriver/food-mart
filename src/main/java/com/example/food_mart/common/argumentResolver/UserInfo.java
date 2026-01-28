package com.example.food_mart.common.argumentResolver;

import com.example.food_mart.modules.user.domain.Role;
import lombok.Getter;

@Getter
public class UserInfo {
    private Long userId;
    private com.example.food_mart.modules.user.domain.Role role;

    protected UserInfo(Long userId, Role role) {
        this.userId = userId;
        this.role = role;
    }
}
