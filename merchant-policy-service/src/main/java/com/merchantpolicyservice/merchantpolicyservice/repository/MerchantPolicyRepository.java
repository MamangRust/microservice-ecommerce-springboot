package com.merchantpolicyservice.merchantpolicyservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.merchantpolicyservice.merchantpolicyservice.entity.MerchantPolicy;
import java.util.List;

public interface MerchantPolicyRepository extends JpaRepository<MerchantPolicy, Long> {
    List<MerchantPolicy> findByMerchantId(Long merchantId);
}