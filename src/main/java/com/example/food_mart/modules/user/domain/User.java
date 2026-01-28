package com.example.food_mart.modules.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nickname; // 필수값, username역할도 함

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private Role role;

    @Builder
    public User(String nickname, Role role) {
        this.nickname = nickname;
        this.role = role;
    }
}
