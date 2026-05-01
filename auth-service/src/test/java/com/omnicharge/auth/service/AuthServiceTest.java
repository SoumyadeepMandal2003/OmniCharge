package com.omnicharge.auth.service;

import com.omnicharge.auth.client.UserServiceClient;
import com.omnicharge.auth.dto.AuthDtos.*;
import com.omnicharge.auth.exception.AuthException;
import com.omnicharge.auth.model.AuthCredential;
import com.omnicharge.auth.model.RefreshToken;
import com.omnicharge.auth.repository.AuthCredentialRepository;
import com.omnicharge.auth.repository.RefreshTokenRepository;
import com.omnicharge.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthCredentialRepository credentialRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks private AuthService authService;

    private AuthCredential sampleCredential;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "authenticationManager", authenticationManager);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "internalSecret", "test-internal-secret");
        sampleCredential = AuthCredential.builder()
                .id(1L).email("user@example.com").password("encoded")
                .role(AuthCredential.Role.USER).userId(10L).enabled(true).build();
    }

    @Test
    void register_success() {
        RegisterRequest req = RegisterRequest.builder()
                .email("user@example.com")
                .password("password123")
                .fullName("John Doe")
                .mobile("9876543210")
                .role("USER")
                .build();

        when(credentialRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
        when(credentialRepository.save(any())).thenReturn(sampleCredential);
        when(userServiceClient.createUserProfile(any(), any())).thenReturn(
                new UserServiceClient.UserProfileResponse(10L, req.getEmail(), "John Doe", "9876543210"));
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access.token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh.token");
        when(refreshTokenRepository.save(any())).thenReturn(new RefreshToken());

        AuthResponse result = authService.register(req);

        assertThat(result.getAccessToken()).isEqualTo("access.token");
        assertThat(result.getUser().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void register_duplicateEmail_throwsAuthException() {
        RegisterRequest req = RegisterRequest.builder()
                .email("user@example.com")
                .password("password123")
                .fullName("John")
                .mobile("9876543210")
                .role("USER")
                .build();
        when(credentialRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest("user@example.com", "password123");
        when(credentialRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(sampleCredential));
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access.token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh.token");
        when(refreshTokenRepository.save(any())).thenReturn(new RefreshToken());

        AuthResponse result = authService.login(req);

        assertThat(result.getAccessToken()).isEqualTo("access.token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials_throwsAuthException() {
        LoginRequest req = new LoginRequest("user@example.com", "wrong");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void validateToken_validToken_returnsValid() {
        when(jwtUtil.isTokenValid("good.token")).thenReturn(true);
        when(jwtUtil.isAccessToken("good.token")).thenReturn(true);
        when(jwtUtil.extractEmail("good.token")).thenReturn("user@example.com");
        when(jwtUtil.extractUserId("good.token")).thenReturn(10L);
        when(jwtUtil.extractRole("good.token")).thenReturn("USER");

        TokenValidationResponse result = authService.validateToken("good.token");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void validateToken_invalidToken_returnsInvalid() {
        when(jwtUtil.isTokenValid("bad.token")).thenReturn(false);

        TokenValidationResponse result = authService.validateToken("bad.token");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void refresh_revokedToken_throwsAuthException() {
        RefreshToken revokedToken = RefreshToken.builder()
                .token("refresh.token").email("user@example.com")
                .revoked(true).expiresAt(LocalDateTime.now().plusDays(1)).build();
        when(refreshTokenRepository.findByToken("refresh.token")).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("refresh.token")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void changePassword_wrongCurrent_throwsAuthException() {
        when(credentialRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleCredential));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword("user@example.com", "wrong", "newpass123"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("incorrect");
    }
}
