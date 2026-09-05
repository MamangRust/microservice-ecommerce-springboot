package com.merchantbusinessservice.merchantbusinessservice.repository;

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

import com.merchantbusinessservice.merchantbusinessservice.entity.MerchantBusinessInformation;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class MerchantBusinessInformationRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MerchantBusinessInformationRepository repository;

    private MerchantBusinessInformation createBusiness(Long merchantId) {
        MerchantBusinessInformation business = new MerchantBusinessInformation();
        business.setMerchantId(merchantId);
        business.setBusinessType("PT");
        business.setTaxId("TAX-" + merchantId);
        business.setEstablishedYear(2020);
        business.setNumberOfEmployees(50);
        business.setWebsiteUrl("https://biz.example.com/" + merchantId);
        return business;
    }

    @Test
    void save_persistsBusinessWithGeneratedIdAndTimestamps() {
        MerchantBusinessInformation saved = repository.save(createBusiness(1L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedBusiness() {
        MerchantBusinessInformation saved = repository.save(createBusiness(1L));

        Optional<MerchantBusinessInformation> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMerchantId()).isEqualTo(1L);
        assertThat(found.get().getBusinessType()).isEqualTo("PT");
        assertThat(found.get().getTaxId()).isEqualTo("TAX-1");
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(repository.findById(999999L)).isEmpty();
    }

    @Test
    void findByMerchantId_returnsSavedBusiness() {
        MerchantBusinessInformation saved = repository.save(createBusiness(1L));

        Optional<MerchantBusinessInformation> found = repository.findByMerchantId(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEstablishedYear()).isEqualTo(2020);
    }

    @Test
    void findByMerchantId_returnsEmptyWhenMissing() {
        assertThat(repository.findByMerchantId(42L)).isEmpty();
    }

    @Test
    void save_secondRowWithSameMerchantId_violatesUniqueConstraint() {
        repository.saveAndFlush(createBusiness(1L));

        assertThatThrownBy(() -> repository.saveAndFlush(createBusiness(1L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        MerchantBusinessInformation saved = repository.save(createBusiness(1L));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setBusinessType("CV");
        MerchantBusinessInformation updated = repository.saveAndFlush(saved);

        assertThat(updated.getBusinessType()).isEqualTo("CV");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        MerchantBusinessInformation saved = repository.save(createBusiness(1L));

        repository.deleteById(saved.getId());
        repository.flush();

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
