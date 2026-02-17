package com.example.food_mart.common.argumentResolver;

import com.example.food_mart.modules.staff.domain.StaffRole;
import lombok.Getter;

@Getter
public class StaffInfo {
    private Long staffId;
    private StaffRole staffRole;

    protected StaffInfo(Long staffId, StaffRole staffRole) {
        this.staffId = staffId;
        this.staffRole = staffRole;
    }
}
