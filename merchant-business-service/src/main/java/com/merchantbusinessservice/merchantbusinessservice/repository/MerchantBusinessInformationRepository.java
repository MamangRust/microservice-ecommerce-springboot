package com.merchantbusinessservice.merchantbusinessservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.merchantbusinessservice.merchantbusinessservice.entity.MerchantBusinessInformation;
import java.util.Optional;

public interface MerchantBusinessInformationRepository extends JpaRepository<MerchantBusinessInformation, Long> {
    Optional<MerchantBusinessInformation> findByMerchantId(Long merchantId);
}