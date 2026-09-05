package com.reviewdetailservice.reviewdetailservice.dto;

import com.reviewdetailservice.reviewdetailservice.entity.ReviewDetail;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:03:46+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class ReviewDetailMapperImpl implements ReviewDetailMapper {

    @Override
    public ReviewDetail toEntity(ReviewDetailRequest request) {
        if ( request == null ) {
            return null;
        }

        ReviewDetail reviewDetail = new ReviewDetail();

        reviewDetail.setReviewId( request.reviewId() );
        reviewDetail.setType( request.type() );
        reviewDetail.setUrl( request.url() );
        reviewDetail.setCaption( request.caption() );

        return reviewDetail;
    }

    @Override
    public ReviewDetailResponse toResponse(ReviewDetail entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long reviewId = null;
        String type = null;
        String url = null;
        String caption = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        reviewId = entity.getReviewId();
        type = entity.getType();
        url = entity.getUrl();
        caption = entity.getCaption();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        ReviewDetailResponse reviewDetailResponse = new ReviewDetailResponse( id, reviewId, type, url, caption, createdAt, updatedAt );

        return reviewDetailResponse;
    }
}
