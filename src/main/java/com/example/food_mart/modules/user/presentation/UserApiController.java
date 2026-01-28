package com.example.food_mart.modules.user.presentation;

import com.example.food_mart.common.ApiResponse;
import com.example.food_mart.common.exception.Expected4xxException;
import com.example.food_mart.common.exception.Expected5xxException;
import com.example.food_mart.modules.user.application.UserSignService;
import com.example.food_mart.modules.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    private final UserSignService userSignService;
    /*
        회원가입
        매개변수 : nickname , role
        리턴 : 성공여부
     */
    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    public ApiResponse signUp(@RequestParam("nickname") String nickname, @RequestParam("role") String role) {
        Map<String,Object> result = userSignService.registerUser(nickname, role);

        if (result.get("failMessage") != null) {
            throw new Expected4xxException(
                    (String)(result.get("failMessage")));
        }

        if (result.get("successValue") == null) {
            throw new Expected5xxException("회원가입에 실패했습니다.");
        }

        return ApiResponse.success();
    }

    /*
        로그인
        매개변수 : nickname
        리턴 : 성공, 실패
     */
    @Operation(summary = "로그인")
    @PostMapping("/sign-in")
    public String signIn(@RequestParam("nickname") String nickname, HttpSession session) {
        User user = userSignService.authenticateUser(nickname);

        String result = null;
        if (user == null) {
            result = "fail";
        } else {
            result = "success";
            session.setAttribute("userId", user.getId());
            session.setAttribute("nickname", user.getNickname());
            session.setAttribute("role", user.getRole());
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
