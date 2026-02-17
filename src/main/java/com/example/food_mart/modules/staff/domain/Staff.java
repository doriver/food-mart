package com.example.food_mart.modules.staff.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nickname; // 필수값, username역할도 함

    @Enumerated(EnumType.STRING)
    @NotNull
    private StaffRole staffRole;

    public Staff(String nickname, StaffRole staffRole) {
        this.nickname = nickname;
        this.staffRole = staffRole;
    }
}
