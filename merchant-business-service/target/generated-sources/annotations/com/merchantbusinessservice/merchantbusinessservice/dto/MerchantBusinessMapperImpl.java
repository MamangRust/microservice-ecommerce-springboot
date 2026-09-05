package com.merchantbusinessservice.merchantbusinessservice.dto;

import com.merchantbusinessservice.merchantbusinessservice.entity.MerchantBusinessInformation;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:02:30+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class MerchantBusinessMapperImpl implements MerchantBusinessMapper {

    @Override
    public MerchantBusinessInformation toEntity(MerchantBusinessRequest request) {
        if ( request == null ) {
            return null;
        }

        MerchantBusinessInformation merchantBusinessInformation = new MerchantBusinessInformation();

        merchantBusinessInformation.setMerchantId( request.merchantId() );
        merchantBusinessInformation.setBusinessType( request.businessType() );
        merchantBusinessInformation.setTaxId( request.taxId() );
        merchantBusinessInformation.setEstablishedYear( request.establishedYear() );
        merchantBusinessInformation.setNumberOfEmployees( request.numberOfEmployees() );
        merchantBusinessInformation.setWebsiteUrl( request.websiteUrl() );

        return merchantBusinessInformation;
    }

    @Override
    public MerchantBusinessResponse toResponse(MerchantBusinessInformation entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long merchantId = null;
        String businessType = null;
        String taxId = null;
        Integer establishedYear = null;
        Integer numberOfEmployees = null;
        String websiteUrl = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        merchantId = entity.getMerchantId();
        businessType = entity.getBusinessType();
        taxId = entity.getTaxId();
        establishedYear = entity.getEstablishedYear();
        numberOfEmployees = entity.getNumberOfEmployees();
        websiteUrl = entity.getWebsiteUrl();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        MerchantBusinessResponse merchantBusinessResponse = new MerchantBusinessResponse( id, merchantId, businessType, taxId, establishedYear, numberOfEmployees, websiteUrl, createdAt, updatedAt );

        return merchantBusinessResponse;
    }
}
