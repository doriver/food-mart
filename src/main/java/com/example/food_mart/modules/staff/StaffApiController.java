package com.example.food_mart.modules.staff;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.common.exception.Expected5xxException;
import com.example.food_mart.modules.staff.application.StaffService;
import com.example.food_mart.modules.staff.domain.Staff;
import com.example.food_mart.modules.staff.domain.StaffRole;
import com.example.food_mart.modules.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffApiController {

    private final StaffService staffService;

    /*
        회원가입
     */
    record StaffCreateDTO(String nickname, StaffRole staffRole) {}

    @PostMapping("/sign-up")
    public ApiResponse signUp(@RequestBody StaffCreateDTO staffCreateDTO) {
        Staff savedStaff = staffService.registerStaff(staffCreateDTO.nickname(), staffCreateDTO.staffRole());
        return ApiResponse.success(savedStaff.getId());
    }

    /*
        로그인
     */
    @PostMapping("/sign-in")
    public String signIn(@RequestParam("nickname") String nickname, HttpSession session) {
        Staff staff = staffService.authenticateStaff(nickname);

        String result = null;
        if (staff == null) {
            result = "fail";
        } else {
            result = "success";
            session.setAttribute("staffId", staff.getId());
            session.setAttribute("nickname", staff.getNickname());
            session.setAttribute("role", staff.getStaffRole());
        }
        return result;
    }

    /*
        로그아웃
     */
    @GetMapping("/sign-out")
    public String signOut(HttpServletRequest request) {
        // 요청에 담긴 세션ID에 해당하는 세션이 있으면 그 세션 반환
        // 없으면 생성X, null반환
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
        return "success";
    }
}
