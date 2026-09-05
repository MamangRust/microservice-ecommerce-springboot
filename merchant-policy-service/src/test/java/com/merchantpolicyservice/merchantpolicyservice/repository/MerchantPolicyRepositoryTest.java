package com.merchantpolicyservice.merchantpolicyservice.repository;

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

import com.merchantpolicyservice.merchantpolicyservice.entity.MerchantPolicy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class MerchantPolicyRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MerchantPolicyRepository repository;

    private MerchantPolicy createPolicy(Long merchantId, String title) {
        MerchantPolicy policy = new MerchantPolicy();
        policy.setMerchantId(merchantId);
        policy.setPolicyType("RETURN");
        policy.setTitle(title);
        policy.setDescription("desc " + title);
        return policy;
    }

    @Test
    void save_persistsPolicyWithGeneratedIdAndTimestamps() {
        MerchantPolicy saved = repository.save(createPolicy(1L, "Policy1"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedPolicy() {
        MerchantPolicy saved = repository.save(createPolicy(1L, "Policy2"));

        Optional<MerchantPolicy> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Policy2");
        assertThat(found.get().getMerchantId()).isEqualTo(1L);
        assertThat(found.get().getPolicyType()).isEqualTo("RETURN");
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(repository.findById(999999L)).isEmpty();
    }

    @Test
    void findByMerchantId_returnsOnlyThatMerchant() {
        repository.save(createPolicy(1L, "Policy1"));
        repository.save(createPolicy(1L, "Policy1b"));
        repository.save(createPolicy(2L, "Policy2"));

        List<MerchantPolicy> result = repository.findByMerchantId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MerchantPolicy::getTitle)
                .containsExactlyInAnyOrder("Policy1", "Policy1b");
    }

    @Test
    void findByMerchantId_returnsEmptyWhenNoMatch() {
        repository.save(createPolicy(1L, "Policy1"));

        assertThat(repository.findByMerchantId(42L)).isEmpty();
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        MerchantPolicy saved = repository.save(createPolicy(1L, "Before"));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setTitle("After");
        saved.setPolicyType("REFUND");
        MerchantPolicy updated = repository.saveAndFlush(saved);

        assertThat(updated.getTitle()).isEqualTo("After");
        assertThat(updated.getPolicyType()).isEqualTo("REFUND");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        MerchantPolicy saved = repository.save(createPolicy(1L, "DeleteMe"));

        repository.deleteById(saved.getId());
        repository.flush();

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
