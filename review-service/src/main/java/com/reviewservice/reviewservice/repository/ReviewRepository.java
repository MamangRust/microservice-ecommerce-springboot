package com.reviewservice.reviewservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.reviewservice.reviewservice.entity.Review;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);
}