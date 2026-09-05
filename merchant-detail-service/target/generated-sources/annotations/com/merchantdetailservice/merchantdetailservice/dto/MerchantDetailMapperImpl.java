package com.merchantdetailservice.merchantdetailservice.dto;

import com.merchantdetailservice.merchantdetailservice.entity.MerchantDetail;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:04:52+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class MerchantDetailMapperImpl implements MerchantDetailMapper {

    @Override
    public MerchantDetail toEntity(MerchantDetailRequest request) {
        if ( request == null ) {
            return null;
        }

        MerchantDetail merchantDetail = new MerchantDetail();

        merchantDetail.setMerchantId( request.merchantId() );
        merchantDetail.setDisplayName( request.displayName() );
        merchantDetail.setCoverImageUrl( request.coverImageUrl() );
        merchantDetail.setLogoUrl( request.logoUrl() );
        merchantDetail.setShortDescription( request.shortDescription() );
        merchantDetail.setWebsiteUrl( request.websiteUrl() );

        return merchantDetail;
    }

    @Override
    public MerchantDetailResponse toResponse(MerchantDetail entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long merchantId = null;
        String displayName = null;
        String coverImageUrl = null;
        String logoUrl = null;
        String shortDescription = null;
        String websiteUrl = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        merchantId = entity.getMerchantId();
        displayName = entity.getDisplayName();
        coverImageUrl = entity.getCoverImageUrl();
        logoUrl = entity.getLogoUrl();
        shortDescription = entity.getShortDescription();
        websiteUrl = entity.getWebsiteUrl();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        MerchantDetailResponse merchantDetailResponse = new MerchantDetailResponse( id, merchantId, displayName, coverImageUrl, logoUrl, shortDescription, websiteUrl, createdAt, updatedAt );

        return merchantDetailResponse;
    }
}
