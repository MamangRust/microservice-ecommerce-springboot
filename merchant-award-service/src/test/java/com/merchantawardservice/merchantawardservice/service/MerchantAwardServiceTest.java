package com.merchantawardservice.merchantawardservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.merchantawardservice.merchantawardservice.dto.MerchantAwardMapper;
import com.merchantawardservice.merchantawardservice.dto.MerchantAwardMapperImpl;
import com.merchantawardservice.merchantawardservice.dto.MerchantAwardRequest;
import com.merchantawardservice.merchantawardservice.entity.MerchantCertificationAndAward;
import com.merchantawardservice.merchantawardservice.repository.MerchantCertificationAndAwardRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class MerchantAwardServiceTest {

    @Mock
    private MerchantCertificationAndAwardRepository repository;

    private MerchantAwardService service;

    private final MerchantAwardMapper mapper = new MerchantAwardMapperImpl();

    @BeforeEach
    void setUp() {
        service = new MerchantAwardService(repository, mapper, OpenTelemetry.noop());
    }

    private MerchantCertificationAndAward createAward(Long id, Long merchantId, String title) {
        MerchantCertificationAndAward award = new MerchantCertificationAndAward();
        award.setId(id);
        award.setMerchantId(merchantId);
        award.setTitle(title);
        award.setDescription("desc " + title);
        award.setIssuedBy("Issuer");
        award.setIssueDate(LocalDate.of(2024, 1, 15));
        award.setExpiryDate(LocalDate.of(2026, 1, 15));
        award.setCertificateUrl("https://cert.example.com/" + id);
        return award;
    }

    private MerchantAwardRequest createRequest(Long merchantId, String title) {
        return new MerchantAwardRequest(merchantId, title, "new desc", "New Issuer",
                LocalDate.of(2025, 2, 1), LocalDate.of(2027, 2, 1), "https://cert.example.com/new");
    }

    @Test
    void getAll_returnsAllFromRepository() {
        when(repository.findAll()).thenReturn(List.of(
                createAward(1L, 1L, "Award1"),
                createAward(2L, 1L, "Award2")));

        List<MerchantCertificationAndAward> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MerchantCertificationAndAward::getTitle)
                .containsExactly("Award1", "Award2");
        verify(repository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNone() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.getAll()).isEmpty();
    }

    @Test
    void getByMerchantId_returnsOnlyThatMerchant() {
        when(repository.findByMerchantId(1L)).thenReturn(List.of(createAward(1L, 1L, "Award1")));

        List<MerchantCertificationAndAward> result = service.getByMerchantId(1L);

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
    void getById_returnsAwardWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(createAward(1L, 1L, "Award1")));

        MerchantCertificationAndAward result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Award1");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Merchant award not found");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        MerchantAwardRequest request = createRequest(1L, "NewAward");
        MerchantCertificationAndAward saved = createAward(5L, 1L, "NewAward");

        when(repository.save(any(MerchantCertificationAndAward.class))).thenReturn(saved);

        MerchantCertificationAndAward result = service.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<MerchantCertificationAndAward> captor =
                ArgumentCaptor.forClass(MerchantCertificationAndAward.class);
        verify(repository).save(captor.capture());
        MerchantCertificationAndAward mapped = captor.getValue();
        assertThat(mapped.getMerchantId()).isEqualTo(1L);
        assertThat(mapped.getTitle()).isEqualTo("NewAward");
        assertThat(mapped.getDescription()).isEqualTo("new desc");
        assertThat(mapped.getIssuedBy()).isEqualTo("New Issuer");
        assertThat(mapped.getIssueDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(mapped.getExpiryDate()).isEqualTo(LocalDate.of(2027, 2, 1));
        assertThat(mapped.getCertificateUrl()).isEqualTo("https://cert.example.com/new");
        assertThat(mapped.getId()).isNull();
    }

    @Test
    void update_updatesFieldsOnExisting() {
        MerchantCertificationAndAward existing = createAward(1L, 1L, "OldTitle");
        MerchantAwardRequest request = createRequest(1L, "UpdatedTitle");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(MerchantCertificationAndAward.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MerchantCertificationAndAward result = service.update(1L, request);

        assertThat(result.getTitle()).isEqualTo("UpdatedTitle");
        assertThat(result.getDescription()).isEqualTo("new desc");
        assertThat(result.getIssuedBy()).isEqualTo("New Issuer");
        assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(result.getExpiryDate()).isEqualTo(LocalDate.of(2027, 2, 1));
        assertThat(result.getCertificateUrl()).isEqualTo("https://cert.example.com/new");
        verify(repository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, createRequest(1L, "X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Merchant award not found");

        verify(repository, never()).save(any(MerchantCertificationAndAward.class));
    }

    @Test
    void delete_delegatesToRepository() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }
}
