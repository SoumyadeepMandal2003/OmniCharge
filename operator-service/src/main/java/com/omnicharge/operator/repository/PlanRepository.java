package com.omnicharge.operator.repository;

import com.omnicharge.operator.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByOperatorIdAndActiveTrue(Long operatorId);
    List<Plan> findByTypeAndActiveTrue(Plan.PlanType type);
    List<Plan> findByActiveTrue();
}
