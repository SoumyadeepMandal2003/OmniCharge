package com.omnicharge.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class UserDtos {

    /**
     * Called internally by auth-service via Feign to create a user profile
     * after credentials have been saved in auth-service.
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateProfileRequest {
        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 2, max = 100)
        private String fullName;

        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        private String mobile;

        @NotBlank
        private String role;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserResponse {
        private Long id;
        private String email;
        private String mobile;
        private String fullName;
        private String role;
        private boolean enabled;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateProfileRequest {
        @Size(min = 2, max = 100)
        private String fullName;

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        private String mobile;
    }
}
