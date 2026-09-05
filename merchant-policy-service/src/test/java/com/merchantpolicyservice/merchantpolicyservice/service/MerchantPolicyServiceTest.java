package com.merchantpolicyservice.merchantpolicyservice.service;

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

import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyMapper;
import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyMapperImpl;
import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyRequest;
import com.merchantpolicyservice.merchantpolicyservice.entity.MerchantPolicy;
import com.merchantpolicyservice.merchantpolicyservice.repository.MerchantPolicyRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class MerchantPolicyServiceTest {

    @Mock
    private MerchantPolicyRepository repository;

    private MerchantPolicyService service;

    private final MerchantPolicyMapper mapper = new MerchantPolicyMapperImpl();

    @BeforeEach
    void setUp() {
        service = new MerchantPolicyService(repository, mapper, OpenTelemetry.noop());
    }

    private MerchantPolicy createPolicy(Long id, Long merchantId, String title) {
        MerchantPolicy policy = new MerchantPolicy();
        policy.setId(id);
        policy.setMerchantId(merchantId);
        policy.setPolicyType("RETURN");
        policy.setTitle(title);
        policy.setDescription("desc " + title);
        return policy;
    }

    private MerchantPolicyRequest createRequest(Long merchantId, String title) {
        return new MerchantPolicyRequest(merchantId, "REFUND", title, "new description");
    }

    @Test
    void getAll_returnsAllFromRepository() {
        when(repository.findAll()).thenReturn(List.of(
                createPolicy(1L, 1L, "Policy1"),
                createPolicy(2L, 2L, "Policy2")));

        List<MerchantPolicy> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MerchantPolicy::getTitle).containsExactly("Policy1", "Policy2");
        verify(repository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNone() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.getAll()).isEmpty();
    }

    @Test
    void getByMerchantId_returnsOnlyThatMerchant() {
        when(repository.findByMerchantId(1L)).thenReturn(List.of(createPolicy(1L, 1L, "Policy1")));

        List<MerchantPolicy> result = service.getByMerchantId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMerchantId()).isEqualTo(1L);
        verify(repository).findByMerchantId(1L);
    }

    @Test
    void getByMerchantId_returnsEmptyWhenNoMatch() {
        when(repository.findByMerchantId(42L)).thenReturn(List.of());

        assertThat(service.getByMerchantId(42L)).isEmpty();
    }

    @Test
    void getById_returnsPolicyWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(createPolicy(1L, 1L, "Policy1")));

        MerchantPolicy result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Policy1");
        assertThat(result.getPolicyType()).isEqualTo("RETURN");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Policy not found");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        MerchantPolicyRequest request = createRequest(1L, "NewPolicy");
        MerchantPolicy saved = createPolicy(5L, 1L, "NewPolicy");

        when(repository.save(any(MerchantPolicy.class))).thenReturn(saved);

        MerchantPolicy result = service.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<MerchantPolicy> captor = ArgumentCaptor.forClass(MerchantPolicy.class);
        verify(repository).save(captor.capture());
        MerchantPolicy mapped = captor.getValue();
        assertThat(mapped.getMerchantId()).isEqualTo(1L);
        assertThat(mapped.getPolicyType()).isEqualTo("REFUND");
        assertThat(mapped.getTitle()).isEqualTo("NewPolicy");
        assertThat(mapped.getDescription()).isEqualTo("new description");
        assertThat(mapped.getId()).isNull();
    }

    @Test
    void update_updatesFieldsOnExisting() {
        MerchantPolicy existing = createPolicy(1L, 1L, "OldTitle");
        MerchantPolicyRequest request = createRequest(1L, "UpdatedPolicy");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(MerchantPolicy.class))).thenAnswer(inv -> inv.getArgument(0));

        MerchantPolicy result = service.update(1L, request);

        assertThat(result.getPolicyType()).isEqualTo("REFUND");
        assertThat(result.getTitle()).isEqualTo("UpdatedPolicy");
        assertThat(result.getDescription()).isEqualTo("new description");
        verify(repository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, createRequest(1L, "X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Policy not found");

        verify(repository, never()).save(any(MerchantPolicy.class));
    }

    @Test
    void delete_delegatesToRepository() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }
}
