package com.omnicharge.recharge.repository;

import com.omnicharge.recharge.model.Recharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RechargeRepository extends JpaRepository<Recharge, Long> {
    List<Recharge> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Recharge> findByRechargeId(String rechargeId);
    List<Recharge> findByStatus(Recharge.RechargeStatus status);
}
