package com.omnicharge.user_service.controller;

import com.omnicharge.user_service.entity.User;
import com.omnicharge.user_service.security.JwtUtil;
import com.omnicharge.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public User register(@RequestBody User user) {

        return userService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {

        User existingUser = userService.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(existingUser.getEmail());
    }
}