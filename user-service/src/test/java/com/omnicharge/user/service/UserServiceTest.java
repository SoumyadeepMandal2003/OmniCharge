package com.omnicharge.user.service;

import com.omnicharge.user.dto.UserDtos.*;
import com.omnicharge.user.exception.DuplicateResourceException;
import com.omnicharge.user.exception.ResourceNotFoundException;
import com.omnicharge.user.model.User;
import com.omnicharge.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L).email("test@example.com").mobile("9876543210")
                .fullName("Test User").role("USER").enabled(true).build();
    }

    @Test
    void createProfile_success() {
        CreateProfileRequest req = new CreateProfileRequest(
                "test@example.com", "Test User", "9876543210", "USER");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(userRepository.existsByMobile(req.getMobile())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse result = userService.createProfile(req);

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createProfile_duplicateEmail_throws() {
        CreateProfileRequest req = new CreateProfileRequest(
                "test@example.com", "Test User", "9876543210", "USER");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createProfile(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getUserByEmail_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        UserResponse result = userService.getUserByEmail("test@example.com");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_success() {
        UpdateProfileRequest req = new UpdateProfileRequest("New Name", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse result = userService.updateProfile(1L, req);
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));
        assertThat(userService.getAllUsers()).hasSize(1);
    }
}
