package com.omnicharge.operator.repository;

import com.omnicharge.operator.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    List<Operator> findByActiveTrue();
    Optional<Operator> findByCode(String code);
    boolean existsByName(String name);
}
