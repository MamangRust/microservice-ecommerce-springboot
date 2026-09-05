package com.reviewdetailservice.reviewdetailservice.repository;

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

import com.reviewdetailservice.reviewdetailservice.entity.ReviewDetail;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ReviewDetailRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ReviewDetailRepository reviewDetailRepository;

    private ReviewDetail createDetail(Long reviewId, String type, String url, String caption) {
        ReviewDetail detail = new ReviewDetail();
        detail.setReviewId(reviewId);
        detail.setType(type);
        detail.setUrl(url);
        detail.setCaption(caption);
        return detail;
    }

    @Test
    void save_persistsDetailWithGeneratedIdAndTimestamps() {
        ReviewDetail saved = reviewDetailRepository.save(createDetail(10L, "image", "http://a.png", "caption1"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedDetail() {
        ReviewDetail saved = reviewDetailRepository.save(createDetail(10L, "video", "http://b.mp4", "caption2"));

        Optional<ReviewDetail> found = reviewDetailRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo("video");
        assertThat(found.get().getReviewId()).isEqualTo(10L);
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        Optional<ReviewDetail> found = reviewDetailRepository.findById(999999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllPersisted() {
        reviewDetailRepository.save(createDetail(10L, "image", "http://a.png", "caption1"));
        reviewDetailRepository.save(createDetail(11L, "video", "http://b.mp4", "caption2"));

        List<ReviewDetail> all = reviewDetailRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(ReviewDetail::getType).containsExactlyInAnyOrder("image", "video");
    }

    @Test
    void findByReviewId_returnsOnlyThatReview() {
        reviewDetailRepository.save(createDetail(10L, "image", "http://a.png", "caption1"));
        reviewDetailRepository.save(createDetail(11L, "video", "http://b.mp4", "caption2"));

        List<ReviewDetail> result = reviewDetailRepository.findByReviewId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("image");
        assertThat(result.get(0).getReviewId()).isEqualTo(10L);
    }

    @Test
    void findByReviewId_returnsEmptyWhenNoMatch() {
        reviewDetailRepository.save(createDetail(10L, "image", "http://a.png", "caption1"));

        List<ReviewDetail> result = reviewDetailRepository.findByReviewId(42L);

        assertThat(result).isEmpty();
    }

    @Test
    void save_acceptsTypeUpTo20Chars() {
        ReviewDetail saved = reviewDetailRepository.save(createDetail(10L, "0123456789012345678", "u", "c"));

        assertThat(saved.getType()).hasSize(19);
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        ReviewDetail saved = reviewDetailRepository.save(createDetail(10L, "image", "http://old.png", "old"));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setCaption("new");
        ReviewDetail updated = reviewDetailRepository.saveAndFlush(saved);

        assertThat(updated.getCaption()).isEqualTo("new");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        ReviewDetail saved = reviewDetailRepository.save(createDetail(10L, "image", "http://a.png", "caption1"));

        reviewDetailRepository.deleteById(saved.getId());
        reviewDetailRepository.flush();

        assertThat(reviewDetailRepository.findById(saved.getId())).isEmpty();
    }
}
