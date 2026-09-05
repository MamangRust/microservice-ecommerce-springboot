package com.reviewservice.reviewservice.dto;

import com.reviewservice.reviewservice.entity.Review;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:01:37+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review toEntity(ReviewRequest request) {
        if ( request == null ) {
            return null;
        }

        Review review = new Review();

        review.setUserId( request.userId() );
        review.setProductId( request.productId() );
        review.setName( request.name() );
        review.setComment( request.comment() );
        review.setRating( request.rating() );

        return review;
    }

    @Override
    public ReviewResponse toResponse(Review entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long userId = null;
        Long productId = null;
        String name = null;
        String comment = null;
        Integer rating = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        userId = entity.getUserId();
        productId = entity.getProductId();
        name = entity.getName();
        comment = entity.getComment();
        rating = entity.getRating();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        ReviewResponse reviewResponse = new ReviewResponse( id, userId, productId, name, comment, rating, createdAt, updatedAt );

        return reviewResponse;
    }
}
