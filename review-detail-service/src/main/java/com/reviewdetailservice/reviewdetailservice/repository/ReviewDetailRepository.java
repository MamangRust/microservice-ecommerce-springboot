package com.reviewdetailservice.reviewdetailservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.reviewdetailservice.reviewdetailservice.entity.ReviewDetail;
import java.util.List;

public interface ReviewDetailRepository extends JpaRepository<ReviewDetail, Long> {
    List<ReviewDetail> findByReviewId(Long reviewId);
}