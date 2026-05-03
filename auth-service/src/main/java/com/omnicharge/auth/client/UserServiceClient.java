package com.omnicharge.auth.client;

import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * Called during registration to create the user profile in user-service.
     * Auth-service owns credentials; user-service owns profile data.
     * The X-Internal-Secret header prevents external callers from hitting this endpoint.
     */
    @PostMapping("/api/users/internal/create")
    UserProfileResponse createUserProfile(
            @RequestHeader("X-Internal-Secret") String internalSecret,
            @RequestBody CreateProfileRequest request);

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class CreateProfileRequest {
        private String email;
        private String fullName;
        private String mobile;
        private String role;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class UserProfileResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("id")
        private Long userId;
        private String email;
        private String fullName;
        private String mobile;
    }
}
