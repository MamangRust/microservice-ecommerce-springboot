package com.reviewdetailservice.reviewdetailservice.service;

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

import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailMapper;
import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailMapperImpl;
import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailRequest;
import com.reviewdetailservice.reviewdetailservice.entity.ReviewDetail;
import com.reviewdetailservice.reviewdetailservice.repository.ReviewDetailRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class ReviewDetailServiceTest {

    @Mock
    private ReviewDetailRepository reviewDetailRepository;

    private ReviewDetailService reviewDetailService;

    private final ReviewDetailMapper reviewDetailMapper = new ReviewDetailMapperImpl();

    @BeforeEach
    void setUp() {
        reviewDetailService = new ReviewDetailService(reviewDetailRepository, reviewDetailMapper, OpenTelemetry.noop());
    }

    private ReviewDetail createDetail(Long id, Long reviewId, String type, String url, String caption) {
        ReviewDetail detail = new ReviewDetail();
        detail.setId(id);
        detail.setReviewId(reviewId);
        detail.setType(type);
        detail.setUrl(url);
        detail.setCaption(caption);
        return detail;
    }

    private ReviewDetailRequest createRequest(Long reviewId, String type, String url, String caption) {
        return new ReviewDetailRequest(reviewId, type, url, caption);
    }

    @Test
    void getAll_returnsAllFromRepository() {
        ReviewDetail d1 = createDetail(1L, 10L, "image", "http://a.png", "caption1");
        ReviewDetail d2 = createDetail(2L, 10L, "video", "http://b.mp4", "caption2");

        when(reviewDetailRepository.findAll()).thenReturn(List.of(d1, d2));

        List<ReviewDetail> result = reviewDetailService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ReviewDetail::getType).containsExactly("image", "video");
        verify(reviewDetailRepository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNoDetails() {
        when(reviewDetailRepository.findAll()).thenReturn(List.of());

        List<ReviewDetail> result = reviewDetailService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getByReviewId_returnsFromRepository() {
        when(reviewDetailRepository.findByReviewId(10L))
                .thenReturn(List.of(createDetail(1L, 10L, "image", "http://a.png", "caption1")));

        List<ReviewDetail> result = reviewDetailService.getByReviewId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReviewId()).isEqualTo(10L);
        verify(reviewDetailRepository).findByReviewId(10L);
    }

    @Test
    void getByReviewId_returnsEmptyWhenNoMatch() {
        when(reviewDetailRepository.findByReviewId(42L)).thenReturn(List.of());

        List<ReviewDetail> result = reviewDetailService.getByReviewId(42L);

        assertThat(result).isEmpty();
    }

    @Test
    void getById_returnsDetailWhenFound() {
        when(reviewDetailRepository.findById(1L))
                .thenReturn(Optional.of(createDetail(1L, 10L, "image", "http://a.png", "caption1")));

        ReviewDetail result = reviewDetailService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo("image");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(reviewDetailRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewDetailService.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Review detail not found");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        ReviewDetailRequest request = createRequest(10L, "image", "http://a.png", "caption1");
        ReviewDetail saved = createDetail(5L, 10L, "image", "http://a.png", "caption1");

        when(reviewDetailRepository.save(any(ReviewDetail.class))).thenReturn(saved);

        ReviewDetail result = reviewDetailService.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<ReviewDetail> captor = ArgumentCaptor.forClass(ReviewDetail.class);
        verify(reviewDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getReviewId()).isEqualTo(10L);
        assertThat(captor.getValue().getType()).isEqualTo("image");
        assertThat(captor.getValue().getUrl()).isEqualTo("http://a.png");
        assertThat(captor.getValue().getCaption()).isEqualTo("caption1");
    }

    @Test
    void update_updatesFieldsOnExisting() {
        ReviewDetail existing = createDetail(1L, 10L, "image", "http://old.png", "old caption");
        ReviewDetailRequest request = createRequest(10L, "video", "http://new.mp4", "new caption");

        when(reviewDetailRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reviewDetailRepository.save(any(ReviewDetail.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDetail result = reviewDetailService.update(1L, request);

        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getUrl()).isEqualTo("http://new.mp4");
        assertThat(result.getCaption()).isEqualTo("new caption");
        assertThat(result.getReviewId()).isEqualTo(10L);
        verify(reviewDetailRepository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(reviewDetailRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewDetailService.update(999L, createRequest(10L, "image", "u", "c")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Review detail not found");

        verify(reviewDetailRepository, never()).save(any(ReviewDetail.class));
    }

    @Test
    void delete_delegatesToRepository() {
        reviewDetailService.delete(1L);

        verify(reviewDetailRepository).deleteById(1L);
    }
}
