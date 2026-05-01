package com.omnicharge.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.user.dto.UserDtos.*;
import com.omnicharge.user.service.UserDashboardService;
import com.omnicharge.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableWebSecurity.class)
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;
    @MockBean private UserDashboardService dashboardService;

    private UserResponse sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserResponse.builder()
                .id(1L).email("test@example.com").mobile("9876543210")
                .fullName("Test User").role("USER").enabled(true).build();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getMyProfile_returnsOk() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(sampleUser);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateProfile_returnsOk() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest("New Name", null);
        when(userService.getUserByEmail("test@example.com")).thenReturn(sampleUser);
        when(userService.updateProfile(eq(1L), any())).thenReturn(sampleUser);

        mockMvc.perform(put("/api/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllUsers_adminRole_returnsOk() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void createProfile_internal_returnsCreated() throws Exception {
        CreateProfileRequest req = new CreateProfileRequest(
                "new@example.com", "New User", "9123456789", "USER");
        when(userService.createProfile(any())).thenReturn(sampleUser);

        mockMvc.perform(post("/api/users/internal/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getUserById_returnsOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
