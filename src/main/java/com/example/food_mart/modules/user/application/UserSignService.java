package com.example.food_mart.modules.user.application;

import com.example.food_mart.modules.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSignService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    /*
        성공시, 저장된 userId값
        실패시, 실패메시지
     */
    public Map<String,Object> registerUser(String nickname, String role) {
        Map<String,Object> result = new HashMap<>();
        Optional<User> optionalUser = userRepository.findByNickname(nickname);
        if (optionalUser.isPresent()) {
            result.put("failMessage", "이미 존재하는 닉네임 입니다");
            return result;
        }

        Role userRole = null;

        if (role.equals("user")) {
            userRole = Role.USER;
        } else if (role.equals("manager")) {
            userRole = Role.MANAGER;
        } else if (role.equals("admin")) {
            userRole = Role.ADMIN;
        }

        User user = User.builder()
                .nickname(nickname).role(userRole)
                .build();
        User savedUser = userRepository.save(user);

        try {
            Wallet wallet = new Wallet(savedUser.getId(), 0L);
            walletRepository.save(wallet);
        } catch (Exception ignored) { }

        result.put("successValue", savedUser.getId());
        return result;
    }

    public User authenticateUser(String nickname) {
        User user = userRepository.findByNickname(nickname).orElse(null);

        return user;
    }
}
