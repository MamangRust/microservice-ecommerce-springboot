package com.merchantdetailservice.merchantdetailservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.merchantdetailservice.merchantdetailservice.entity.MerchantDetail;
import java.util.Optional;

public interface MerchantDetailRepository extends JpaRepository<MerchantDetail, Long> {
    Optional<MerchantDetail> findByMerchantId(Long merchantId);
}