package com.merchantpolicyservice.merchantpolicyservice.dto;

import com.merchantpolicyservice.merchantpolicyservice.entity.MerchantPolicy;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:06:22+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class MerchantPolicyMapperImpl implements MerchantPolicyMapper {

    @Override
    public MerchantPolicy toEntity(MerchantPolicyRequest request) {
        if ( request == null ) {
            return null;
        }

        MerchantPolicy merchantPolicy = new MerchantPolicy();

        merchantPolicy.setMerchantId( request.merchantId() );
        merchantPolicy.setPolicyType( request.policyType() );
        merchantPolicy.setTitle( request.title() );
        merchantPolicy.setDescription( request.description() );

        return merchantPolicy;
    }

    @Override
    public MerchantPolicyResponse toResponse(MerchantPolicy entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long merchantId = null;
        String policyType = null;
        String title = null;
        String description = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        merchantId = entity.getMerchantId();
        policyType = entity.getPolicyType();
        title = entity.getTitle();
        description = entity.getDescription();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        MerchantPolicyResponse merchantPolicyResponse = new MerchantPolicyResponse( id, merchantId, policyType, title, description, createdAt, updatedAt );

        return merchantPolicyResponse;
    }
}
