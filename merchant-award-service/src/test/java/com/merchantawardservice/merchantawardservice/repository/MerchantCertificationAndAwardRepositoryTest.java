package com.merchantawardservice.merchantawardservice.repository;

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

import com.merchantawardservice.merchantawardservice.entity.MerchantCertificationAndAward;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class MerchantCertificationAndAwardRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MerchantCertificationAndAwardRepository repository;

    private MerchantCertificationAndAward createAward(Long merchantId, String title) {
        MerchantCertificationAndAward award = new MerchantCertificationAndAward();
        award.setMerchantId(merchantId);
        award.setTitle(title);
        award.setIssuedBy("Issuer");
        award.setIssueDate(LocalDateTime.now().toLocalDate());
        award.setExpiryDate(LocalDateTime.now().toLocalDate().plusYears(1));
        return award;
    }

    @Test
    void save_persistsAwardWithGeneratedIdAndTimestamps() {
        MerchantCertificationAndAward saved = repository.save(createAward(1L, "Award1"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedAward() {
        MerchantCertificationAndAward saved = repository.save(createAward(1L, "Award2"));

        Optional<MerchantCertificationAndAward> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Award2");
        assertThat(found.get().getMerchantId()).isEqualTo(1L);
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(repository.findById(999999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllPersisted() {
        repository.save(createAward(1L, "Award1"));
        repository.save(createAward(2L, "Award2"));

        List<MerchantCertificationAndAward> all = repository.findAll();

        assertThat(all).extracting(MerchantCertificationAndAward::getTitle)
                .contains("Award1", "Award2");
    }

    @Test
    void findByMerchantId_returnsOnlyThatMerchant() {
        repository.save(createAward(1L, "Award1"));
        repository.save(createAward(1L, "Award1b"));
        repository.save(createAward(2L, "Award2"));

        List<MerchantCertificationAndAward> result = repository.findByMerchantId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(award -> assertThat(award.getMerchantId()).isEqualTo(1L));
    }

    @Test
    void findByMerchantId_returnsEmptyWhenNoMatch() {
        repository.save(createAward(1L, "Award1"));

        assertThat(repository.findByMerchantId(42L)).isEmpty();
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        MerchantCertificationAndAward saved = repository.save(createAward(1L, "Before"));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setTitle("After");
        MerchantCertificationAndAward updated = repository.saveAndFlush(saved);

        assertThat(updated.getTitle()).isEqualTo("After");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        MerchantCertificationAndAward saved = repository.save(createAward(1L, "DeleteMe"));

        repository.deleteById(saved.getId());
        repository.flush();

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
