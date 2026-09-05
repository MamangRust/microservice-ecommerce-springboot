package com.bannerservice.bannerservice.repository;

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

import com.bannerservice.bannerservice.entity.Banner;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class BannerRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private BannerRepository bannerRepository;

    private Banner createBanner(String name, Boolean isActive) {
        Banner banner = new Banner();
        banner.setName(name);
        if (isActive != null) {
            banner.setIsActive(isActive);
        }
        return banner;
    }

    @Test
    void save_persistsBannerWithGeneratedIdAndTimestamps() {
        Banner saved = bannerRepository.save(createBanner("Banner1", null));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedBanner() {
        Banner saved = bannerRepository.save(createBanner("Banner2", true));

        Optional<Banner> found = bannerRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Banner2");
        assertThat(found.get().getIsActive()).isTrue();
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(bannerRepository.findById(999999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllPersisted() {
        bannerRepository.save(createBanner("Banner1", true));
        bannerRepository.save(createBanner("Banner2", true));

        List<Banner> all = bannerRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Banner::getName).containsExactlyInAnyOrder("Banner1", "Banner2");
    }

    @Test
    void findByIsActiveTrue_returnsOnlyActive() {
        Banner active = bannerRepository.save(createBanner("ActiveBanner", true));
        bannerRepository.save(createBanner("InactiveBanner", false));

        List<Banner> result = bannerRepository.findByIsActiveTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(active.getId());
        assertThat(result.get(0).getName()).isEqualTo("ActiveBanner");
    }

    @Test
    void findByIsActiveTrue_returnsEmptyWhenNoActiveBanners() {
        bannerRepository.save(createBanner("InactiveBanner", false));

        assertThat(bannerRepository.findByIsActiveTrue()).isEmpty();
    }

    @Test
    void update_setInactive_removesFromActiveListAndTouchesUpdatedAt() {
        Banner saved = bannerRepository.save(createBanner("Before", true));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setIsActive(false);
        Banner updated = bannerRepository.saveAndFlush(saved);

        assertThat(updated.getIsActive()).isFalse();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(bannerRepository.findByIsActiveTrue())
                .extracting(Banner::getId)
                .doesNotContain(saved.getId());
    }

    @Test
    void deleteById_removesRow() {
        Banner saved = bannerRepository.save(createBanner("DeleteMe", true));

        bannerRepository.deleteById(saved.getId());
        bannerRepository.flush();

        assertThat(bannerRepository.findById(saved.getId())).isEmpty();
    }
}
