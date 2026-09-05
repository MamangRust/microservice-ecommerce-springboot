package com.reviewservice.reviewservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.reviewservice.reviewservice.entity.Review;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ReviewRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ReviewRepository reviewRepository;

    private Review createReview(Long userId, Long productId, String name, Integer rating) {
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setName(name);
        review.setComment("comment");
        review.setRating(rating);
        return review;
    }

    @Test
    void save_persistsReviewWithGeneratedIdAndTimestamps() {
        Review saved = reviewRepository.save(createReview(1L, 5L, "Review1", 5));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedReview() {
        Review saved = reviewRepository.save(createReview(1L, 5L, "Review2", 4));

        Optional<Review> found = reviewRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Review2");
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getProductId()).isEqualTo(5L);
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        Optional<Review> found = reviewRepository.findById(999999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllPersisted() {
        reviewRepository.save(createReview(1L, 5L, "Review1", 5));
        reviewRepository.save(createReview(2L, 6L, "Review2", 4));

        List<Review> all = reviewRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Review::getName).containsExactlyInAnyOrder("Review1", "Review2");
    }

    @Test
    void findByProductId_returnsOnlyThatProduct() {
        reviewRepository.save(createReview(1L, 5L, "Product5Review", 5));
        reviewRepository.save(createReview(2L, 6L, "Product6Review", 4));

        List<Review> result = reviewRepository.findByProductId(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Product5Review");
        assertThat(result.get(0).getProductId()).isEqualTo(5L);
    }

    @Test
    void findByProductId_returnsEmptyWhenNoMatch() {
        reviewRepository.save(createReview(1L, 5L, "Product5Review", 5));

        List<Review> result = reviewRepository.findByProductId(42L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_returnsOnlyThatUser() {
        reviewRepository.save(createReview(1L, 5L, "User1Review", 5));
        reviewRepository.save(createReview(2L, 6L, "User2Review", 4));

        List<Review> result = reviewRepository.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("User1Review");
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        Review saved = reviewRepository.save(createReview(1L, 5L, "Before", 3));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setName("After");
        Review updated = reviewRepository.saveAndFlush(saved);

        assertThat(updated.getName()).isEqualTo("After");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        Review saved = reviewRepository.save(createReview(1L, 5L, "DeleteMe", 5));

        reviewRepository.deleteById(saved.getId());
        reviewRepository.flush();

        assertThat(reviewRepository.findById(saved.getId())).isEmpty();
    }
}
