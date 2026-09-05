package com.reviewservice.reviewservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reviewservice.reviewservice.dto.ReviewMapper;
import com.reviewservice.reviewservice.dto.ReviewMapperImpl;
import com.reviewservice.reviewservice.dto.ReviewRequest;
import com.reviewservice.reviewservice.entity.Review;
import com.reviewservice.reviewservice.repository.ReviewRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    private ReviewService reviewService;

    private final ReviewMapper reviewMapper = new ReviewMapperImpl();

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, reviewMapper, OpenTelemetry.noop());
    }

    private Review createReview(Long id, Long userId, Long productId, String name, Integer rating) {
        Review review = new Review();
        review.setId(id);
        review.setUserId(userId);
        review.setProductId(productId);
        review.setName(name);
        review.setComment("comment");
        review.setRating(rating);
        return review;
    }

    private ReviewRequest createRequest(Long userId, Long productId, String name, Integer rating) {
        return new ReviewRequest(userId, productId, name, "comment", rating);
    }

    @Test
    void getAll_returnsAllFromRepository() {
        Review r1 = createReview(1L, 1L, 5L, "Review1", 5);
        Review r2 = createReview(2L, 2L, 5L, "Review2", 4);

        when(reviewRepository.findAll()).thenReturn(List.of(r1, r2));

        List<Review> result = reviewService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Review::getName).containsExactly("Review1", "Review2");
        verify(reviewRepository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNoReviews() {
        when(reviewRepository.findAll()).thenReturn(List.of());

        List<Review> result = reviewService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getByProductId_returnsFromRepository() {
        when(reviewRepository.findByProductId(5L))
                .thenReturn(List.of(createReview(1L, 1L, 5L, "ProductReview", 5)));

        List<Review> result = reviewService.getByProductId(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(5L);
        verify(reviewRepository).findByProductId(5L);
    }

    @Test
    void getByProductId_returnsEmptyWhenNoMatch() {
        when(reviewRepository.findByProductId(42L)).thenReturn(List.of());

        List<Review> result = reviewService.getByProductId(42L);

        assertThat(result).isEmpty();
    }

    @Test
    void getById_returnsReviewWhenFound() {
        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(createReview(1L, 1L, 5L, "Review1", 5)));

        Review result = reviewService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Review1");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Review not found");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        ReviewRequest request = createRequest(1L, 5L, "NewReview", 4);
        Review saved = createReview(5L, 1L, 5L, "NewReview", 4);

        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        Review result = reviewService.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getProductId()).isEqualTo(5L);
        assertThat(captor.getValue().getName()).isEqualTo("NewReview");
        assertThat(captor.getValue().getRating()).isEqualTo(4);
    }

    @Test
    void update_updatesFieldsOnExisting() {
        Review existing = createReview(1L, 1L, 5L, "OldName", 3);
        ReviewRequest request = createRequest(1L, 5L, "UpdatedName", 5);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review result = reviewService.update(1L, request);

        assertThat(result.getName()).isEqualTo("UpdatedName");
        assertThat(result.getComment()).isEqualTo("comment");
        assertThat(result.getRating()).isEqualTo(5);
        verify(reviewRepository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.update(999L, createRequest(1L, 5L, "X", 1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Review not found");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void delete_delegatesToRepository() {
        reviewService.delete(1L);

        verify(reviewRepository).deleteById(1L);
    }
}
