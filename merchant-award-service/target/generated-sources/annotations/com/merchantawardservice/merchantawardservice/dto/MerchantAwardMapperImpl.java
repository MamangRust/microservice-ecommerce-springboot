package com.merchantawardservice.merchantawardservice.dto;

import com.merchantawardservice.merchantawardservice.entity.MerchantCertificationAndAward;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:00:42+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class MerchantAwardMapperImpl implements MerchantAwardMapper {

    @Override
    public MerchantCertificationAndAward toEntity(MerchantAwardRequest request) {
        if ( request == null ) {
            return null;
        }

        MerchantCertificationAndAward merchantCertificationAndAward = new MerchantCertificationAndAward();

        merchantCertificationAndAward.setMerchantId( request.merchantId() );
        merchantCertificationAndAward.setTitle( request.title() );
        merchantCertificationAndAward.setDescription( request.description() );
        merchantCertificationAndAward.setIssuedBy( request.issuedBy() );
        merchantCertificationAndAward.setIssueDate( request.issueDate() );
        merchantCertificationAndAward.setExpiryDate( request.expiryDate() );
        merchantCertificationAndAward.setCertificateUrl( request.certificateUrl() );

        return merchantCertificationAndAward;
    }

    @Override
    public MerchantAwardResponse toResponse(MerchantCertificationAndAward entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long merchantId = null;
        String title = null;
        String description = null;
        String issuedBy = null;
        LocalDate issueDate = null;
        LocalDate expiryDate = null;
        String certificateUrl = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        merchantId = entity.getMerchantId();
        title = entity.getTitle();
        description = entity.getDescription();
        issuedBy = entity.getIssuedBy();
        issueDate = entity.getIssueDate();
        expiryDate = entity.getExpiryDate();
        certificateUrl = entity.getCertificateUrl();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        MerchantAwardResponse merchantAwardResponse = new MerchantAwardResponse( id, merchantId, title, description, issuedBy, issueDate, expiryDate, certificateUrl, createdAt, updatedAt );

        return merchantAwardResponse;
    }
}
