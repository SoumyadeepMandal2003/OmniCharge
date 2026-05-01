package com.omnicharge.user.service;

import com.omnicharge.user.dto.UserDtos.*;
import com.omnicharge.user.exception.DuplicateResourceException;
import com.omnicharge.user.exception.ResourceNotFoundException;
import com.omnicharge.user.model.User;
import com.omnicharge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Called internally by auth-service via Feign after credentials are saved.
     * Creates the user profile record in user-service DB.
     */
    @Transactional
    public UserResponse createProfile(CreateProfileRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Profile already exists for: " + request.getEmail());
        }
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new DuplicateResourceException("Mobile number already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .mobile(request.getMobile())
                .role(request.getRole())
                .enabled(true)
                .build();
        User saved = userRepository.save(user);
        log.info("Created profile for user {}", request.getEmail());
        return toResponse(saved);
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getMobile() != null) {
            if (userRepository.existsByMobile(request.getMobile())
                    && !user.getMobile().equals(request.getMobile())) {
                throw new DuplicateResourceException("Mobile already in use");
            }
            user.setMobile(request.getMobile());
        }
        return toResponse(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}
