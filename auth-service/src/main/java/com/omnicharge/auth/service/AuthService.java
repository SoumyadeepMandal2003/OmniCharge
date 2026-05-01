package com.omnicharge.auth.service;

import com.omnicharge.auth.client.UserServiceClient;
import com.omnicharge.auth.dto.AuthDtos.*;
import com.omnicharge.auth.exception.AuthException;
import com.omnicharge.auth.model.AuthCredential;
import com.omnicharge.auth.model.RefreshToken;
import com.omnicharge.auth.repository.AuthCredentialRepository;
import com.omnicharge.auth.repository.RefreshTokenRepository;
import com.omnicharge.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final AuthCredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;
    
    private final UserServiceClient userServiceClient;

    @Value("${internal.secret:omnicharge-internal-secret-2024}")
    private String internalSecret;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered: " + request.getEmail());
        }

        AuthCredential.Role requestedRole = AuthCredential.Role.USER;

        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            requestedRole = AuthCredential.Role.ADMIN;
        }

        // Step 1 — save credentials in auth-service DB
        AuthCredential credential = AuthCredential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(requestedRole)
                .enabled(true)
                .build();
        credential = credentialRepository.save(credential);

        // Step 2 — create profile in user-service via Feign
        try {
            UserServiceClient.UserProfileResponse profile = userServiceClient.createUserProfile(
                    internalSecret,
                    UserServiceClient.CreateProfileRequest.builder()
                            .email(request.getEmail())
                            .fullName(request.getFullName())
                            .mobile(request.getMobile())
                            .role(credential.getRole().name())
                            .build()
            );

            // Step 3 — store the userId returned by user-service
            credential.setUserId(profile.getUserId());
            credentialRepository.save(credential);

            log.info("Registered user {} with userId {}", request.getEmail(), profile.getUserId());
            return buildAuthResponse(credential);

        } catch (Exception e) {
            // Rollback credential if user-service call fails
            credentialRepository.delete(credential);
            log.error("Failed to create profile in user-service for {}: {}", request.getEmail(), e.getMessage());
            throw new AuthException("Registration failed: unable to create user profile. Please try again.");
        }
    }

    //Login the user
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new AuthException("Invalid email or password");
        }

        AuthCredential credential = credentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("User not found"));

        if (!credential.isEnabled()) {
            throw new AuthException("Account is disabled");
        }

        log.info("User {} logged in", request.getEmail());
        return buildAuthResponse(credential);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (stored.isRevoked()) {
            throw new AuthException("Refresh token has been revoked");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException("Refresh token has expired");
        }
        if (!jwtUtil.isTokenValid(request.getRefreshToken())) {
            throw new AuthException("Refresh token is invalid");
        }

        // Revoke old refresh token and issue a new one
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        AuthCredential credential = credentialRepository.findByEmail(stored.getEmail())
                .orElseThrow(() -> new AuthException("User not found"));

        return buildAuthResponse(credential);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Logged out user {}", token.getEmail());
        });
    }

    @Transactional
    public void logoutAll(String email) {
        refreshTokenRepository.revokeAllByEmail(email);
        log.info("Revoked all sessions for {}", email);
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            if (!jwtUtil.isTokenValid(token) || !jwtUtil.isAccessToken(token)) {
                return TokenValidationResponse.builder().valid(false).build();
            }
            return TokenValidationResponse.builder()
                    .valid(true)
                    .email(jwtUtil.extractEmail(token))
                    .userId(jwtUtil.extractUserId(token))
                    .role(jwtUtil.extractRole(token))
                    .build();
        } catch (Exception e) {
            return TokenValidationResponse.builder().valid(false).build();
        }
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        AuthCredential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        if (!passwordEncoder.matches(currentPassword, credential.getPassword())) {
            throw new AuthException("Current password is incorrect");
        }

        credential.setPassword(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);

        // Revoke all refresh tokens to force re-login on all devices
        refreshTokenRepository.revokeAllByEmail(email);
        log.info("Password changed for {}", email);
    }



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthCredential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new org.springframework.security.core.userdetails.User(
                credential.getEmail(),
                credential.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + credential.getRole().name())));
    }

    private AuthResponse buildAuthResponse(AuthCredential credential) {
        String accessToken = jwtUtil.generateAccessToken(
                credential.getEmail(),
                credential.getUserId(),
                credential.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(credential.getEmail());

        // Persist refresh token
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .email(credential.getEmail())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .user(UserInfo.builder()
                        .userId(credential.getUserId())
                        .email(credential.getEmail())
                        .role(credential.getRole().name())
                        .build())
                .build();
    }
}
