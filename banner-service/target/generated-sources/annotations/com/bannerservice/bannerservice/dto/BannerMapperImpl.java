package com.bannerservice.bannerservice.dto;

import com.bannerservice.bannerservice.entity.Banner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T12:53:57+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class BannerMapperImpl implements BannerMapper {

    @Override
    public Banner toEntity(BannerRequest request) {
        if ( request == null ) {
            return null;
        }

        Banner banner = new Banner();

        banner.setName( request.name() );
        banner.setStartDate( request.startDate() );
        banner.setEndDate( request.endDate() );
        banner.setStartTime( request.startTime() );
        banner.setEndTime( request.endTime() );
        banner.setIsActive( request.isActive() );

        return banner;
    }

    @Override
    public BannerResponse toResponse(Banner entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalTime startTime = null;
        LocalTime endTime = null;
        Boolean isActive = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        name = entity.getName();
        startDate = entity.getStartDate();
        endDate = entity.getEndDate();
        startTime = entity.getStartTime();
        endTime = entity.getEndTime();
        isActive = entity.getIsActive();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        BannerResponse bannerResponse = new BannerResponse( id, name, startDate, endDate, startTime, endTime, isActive, createdAt, updatedAt );

        return bannerResponse;
    }
}
