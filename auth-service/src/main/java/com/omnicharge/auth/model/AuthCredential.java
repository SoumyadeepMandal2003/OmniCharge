package com.omnicharge.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Stores ONLY authentication credentials.
 * Profile data (fullName, mobile, etc.) lives in user-service.
 */
@Entity
@Table(name = "auth_credentials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * The userId from user-service.
     * Set after auth-service calls user-service to create the profile.
     */
    @Column(unique = true)
    private Long userId;

    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public enum Role {
        USER, ADMIN
    }
}
