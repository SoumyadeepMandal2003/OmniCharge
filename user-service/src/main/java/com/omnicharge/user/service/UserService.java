package com.omnicharge.user.service;

import com.omnicharge.user.client.PaymentClient;
import com.omnicharge.user.client.RechargeClient;
import com.omnicharge.user.dto.UserDtos.*;
import com.omnicharge.user.exception.DuplicateResourceException;
import com.omnicharge.user.exception.ResourceNotFoundException;
import com.omnicharge.user.model.User;
import com.omnicharge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RechargeClient rechargeClient;
    private final PaymentClient paymentClient;

    @Value("${internal.secret:omnicharge-internal-secret-2024}")
    private String internalSecret;

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

    /**
     * Called internally by auth-service to delete all user data.
     * Deletes recharges and transactions from downstream services, then removes the user profile.
     */
    @Transactional
    public void deleteAccount(Long userId, String secret) {
        if (!internalSecret.equals(secret)) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid internal secret");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Delete recharge history from recharge-service
        try {
            rechargeClient.deleteAllRechargesForUser(userId, internalSecret);
        } catch (Exception e) {
            log.warn("Failed to delete recharges for userId={}: {}", userId, e.getMessage());
        }

        // Delete transactions from payment-service
        try {
            paymentClient.deleteAllTransactionsForUser(userId, internalSecret);
        } catch (Exception e) {
            log.warn("Failed to delete transactions for userId={}: {}", userId, e.getMessage());
        }

        // Delete user profile
        userRepository.delete(user);
        log.info("Deleted account and all data for userId={}, email={}", userId, user.getEmail());
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
