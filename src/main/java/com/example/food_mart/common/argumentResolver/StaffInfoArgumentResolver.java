package com.example.food_mart.common.argumentResolver;

import com.example.food_mart.modules.staff.domain.StaffRole;
import com.example.food_mart.modules.user.domain.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class StaffInfoArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return StaffInfo.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        Long staffId = null;
        StaffRole staffRole = null;
        /*
            로그인된 직원 정보 가져오기
            일단 인증부분은 Security없이 session으로만 구현함
            todo : Security 적용하기
         */
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpSession session = request.getSession(false);

        if (session != null) {
            staffId = (Long) session.getAttribute("staffId");
            staffRole = (StaffRole) session.getAttribute("role");
        }

        return new StaffInfo(staffId, staffRole);
    }
}
