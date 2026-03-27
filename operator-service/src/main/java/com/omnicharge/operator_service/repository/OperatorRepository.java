package com.omnicharge.operator_service.repository;

import com.omnicharge.operator_service.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorRepository extends JpaRepository<Operator, Long> {
}