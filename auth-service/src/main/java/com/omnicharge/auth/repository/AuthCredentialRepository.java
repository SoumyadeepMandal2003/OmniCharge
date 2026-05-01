package com.omnicharge.auth.repository;

import com.omnicharge.auth.model.AuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthCredentialRepository extends JpaRepository<AuthCredential, Long> {
    Optional<AuthCredential> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<AuthCredential> findByUserId(Long userId);
}
