package com.example.food_mart.modules.staff.application;

import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.modules.staff.domain.Staff;
import com.example.food_mart.modules.staff.domain.StaffRepository;
import com.example.food_mart.modules.staff.domain.StaffRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    public Staff registerStaff(String nickname, StaffRole staffRole) {
        Optional<Staff> optionalStaff = staffRepository.findByNickname(nickname);
        if (optionalStaff.isPresent()) {
            throw new Expected4xxException("이미 존재하는 닉네임 입니다.");
        }

        Staff savedStaff = staffRepository.save(new Staff(nickname, staffRole));
        return savedStaff;
    }

    public Staff authenticateStaff(String nickname) {
        Staff staff = staffRepository.findByNickname(nickname)
                .orElseThrow(() -> new Expected4xxException("이미 존재하는 닉네임 입니다."));
        return staff;
    }
}
