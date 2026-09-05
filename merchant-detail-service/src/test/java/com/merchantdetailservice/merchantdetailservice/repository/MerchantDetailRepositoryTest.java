package com.merchantdetailservice.merchantdetailservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.merchantdetailservice.merchantdetailservice.entity.MerchantDetail;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class MerchantDetailRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MerchantDetailRepository repository;

    private MerchantDetail createDetail(Long merchantId) {
        MerchantDetail detail = new MerchantDetail();
        detail.setMerchantId(merchantId);
        detail.setDisplayName("Merchant " + merchantId);
        detail.setCoverImageUrl("https://img.example.com/cover/" + merchantId);
        detail.setLogoUrl("https://img.example.com/logo/" + merchantId);
        detail.setShortDescription("short desc " + merchantId);
        detail.setWebsiteUrl("https://merchant.example.com/" + merchantId);
        return detail;
    }

    @Test
    void save_persistsDetailWithGeneratedIdAndTimestamps() {
        MerchantDetail saved = repository.save(createDetail(1L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedDetail() {
        MerchantDetail saved = repository.save(createDetail(1L));

        Optional<MerchantDetail> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMerchantId()).isEqualTo(1L);
        assertThat(found.get().getDisplayName()).isEqualTo("Merchant 1");
        assertThat(found.get().getShortDescription()).isEqualTo("short desc 1");
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(repository.findById(999999L)).isEmpty();
    }

    @Test
    void findByMerchantId_returnsSavedDetail() {
        MerchantDetail saved = repository.save(createDetail(1L));

        Optional<MerchantDetail> found = repository.findByMerchantId(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getWebsiteUrl()).isEqualTo("https://merchant.example.com/1");
    }

    @Test
    void findByMerchantId_returnsEmptyWhenMissing() {
        assertThat(repository.findByMerchantId(42L)).isEmpty();
    }

    @Test
    void save_secondRowWithSameMerchantId_violatesUniqueConstraint() {
        repository.saveAndFlush(createDetail(1L));

        assertThatThrownBy(() -> repository.saveAndFlush(createDetail(1L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        MerchantDetail saved = repository.save(createDetail(1L));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setDisplayName("Updated Name");
        MerchantDetail updated = repository.saveAndFlush(saved);

        assertThat(updated.getDisplayName()).isEqualTo("Updated Name");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        MerchantDetail saved = repository.save(createDetail(1L));

        repository.deleteById(saved.getId());
        repository.flush();

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
