package com.merchantawardservice.merchantawardservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.merchantawardservice.merchantawardservice.entity.MerchantCertificationAndAward;

import java.util.List;

public interface MerchantCertificationAndAwardRepository extends JpaRepository<MerchantCertificationAndAward, Long> {
    List<MerchantCertificationAndAward> findByMerchantId(Long merchantId);
}