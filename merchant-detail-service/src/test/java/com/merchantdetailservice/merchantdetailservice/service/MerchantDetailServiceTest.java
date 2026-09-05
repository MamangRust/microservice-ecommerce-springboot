package com.merchantdetailservice.merchantdetailservice.service;

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

import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailMapper;
import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailMapperImpl;
import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailRequest;
import com.merchantdetailservice.merchantdetailservice.entity.MerchantDetail;
import com.merchantdetailservice.merchantdetailservice.repository.MerchantDetailRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class MerchantDetailServiceTest {

    @Mock
    private MerchantDetailRepository repository;

    private MerchantDetailService service;

    private final MerchantDetailMapper mapper = new MerchantDetailMapperImpl();

    @BeforeEach
    void setUp() {
        service = new MerchantDetailService(repository, mapper, OpenTelemetry.noop());
    }

    private MerchantDetail createDetail(Long id, Long merchantId) {
        MerchantDetail detail = new MerchantDetail();
        detail.setId(id);
        detail.setMerchantId(merchantId);
        detail.setDisplayName("Merchant " + merchantId);
        detail.setCoverImageUrl("https://img.example.com/cover/" + merchantId);
        detail.setLogoUrl("https://img.example.com/logo/" + merchantId);
        detail.setShortDescription("short desc " + merchantId);
        detail.setWebsiteUrl("https://merchant.example.com/" + merchantId);
        return detail;
    }

    private MerchantDetailRequest createRequest(Long merchantId) {
        return new MerchantDetailRequest(merchantId, "New Display", "https://new.example.com/cover",
                "https://new.example.com/logo", "new short desc", "https://new.example.com");
    }

    @Test
    void getAll_returnsAllFromRepository() {
        when(repository.findAll()).thenReturn(List.of(createDetail(1L, 1L), createDetail(2L, 2L)));

        List<MerchantDetail> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MerchantDetail::getMerchantId).containsExactly(1L, 2L);
        verify(repository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNone() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.getAll()).isEmpty();
    }

    @Test
    void getByMerchantId_returnsDetailWhenFound() {
        when(repository.findByMerchantId(1L)).thenReturn(Optional.of(createDetail(1L, 1L)));

        MerchantDetail result = service.getByMerchantId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMerchantId()).isEqualTo(1L);
        assertThat(result.getDisplayName()).isEqualTo("Merchant 1");
    }

    @Test
    void getByMerchantId_throwsWhenNotFound() {
        when(repository.findByMerchantId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByMerchantId(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Merchant detail not found");
    }

    @Test
    void getById_returnsDetailWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(createDetail(1L, 1L)));

        MerchantDetail result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Merchant detail not found");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        MerchantDetailRequest request = createRequest(1L);
        MerchantDetail saved = createDetail(5L, 1L);

        when(repository.save(any(MerchantDetail.class))).thenReturn(saved);

        MerchantDetail result = service.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<MerchantDetail> captor = ArgumentCaptor.forClass(MerchantDetail.class);
        verify(repository).save(captor.capture());
        MerchantDetail mapped = captor.getValue();
        assertThat(mapped.getMerchantId()).isEqualTo(1L);
        assertThat(mapped.getDisplayName()).isEqualTo("New Display");
        assertThat(mapped.getCoverImageUrl()).isEqualTo("https://new.example.com/cover");
        assertThat(mapped.getLogoUrl()).isEqualTo("https://new.example.com/logo");
        assertThat(mapped.getShortDescription()).isEqualTo("new short desc");
        assertThat(mapped.getWebsiteUrl()).isEqualTo("https://new.example.com");
        assertThat(mapped.getId()).isNull();
    }

    @Test
    void update_updatesFieldsOnExisting() {
        MerchantDetail existing = createDetail(1L, 1L);
        MerchantDetailRequest request = createRequest(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(MerchantDetail.class))).thenAnswer(inv -> inv.getArgument(0));

        MerchantDetail result = service.update(1L, request);

        assertThat(result.getDisplayName()).isEqualTo("New Display");
        assertThat(result.getCoverImageUrl()).isEqualTo("https://new.example.com/cover");
        assertThat(result.getLogoUrl()).isEqualTo("https://new.example.com/logo");
        assertThat(result.getShortDescription()).isEqualTo("new short desc");
        assertThat(result.getWebsiteUrl()).isEqualTo("https://new.example.com");
        verify(repository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, createRequest(1L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Merchant detail not found");

        verify(repository, never()).save(any(MerchantDetail.class));
    }

    @Test
    void delete_delegatesToRepository() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }
}
