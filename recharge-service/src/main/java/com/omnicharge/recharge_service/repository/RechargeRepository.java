package com.omnicharge.recharge_service.repository;

import com.omnicharge.recharge_service.entity.Recharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RechargeRepository extends JpaRepository<Recharge, Long> {

    List<Recharge> findByMobileNumber(String mobileNumber);

}