package com.merchantbusinessservice.merchantbusinessservice.service;

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

import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessMapper;
import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessMapperImpl;
import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessRequest;
import com.merchantbusinessservice.merchantbusinessservice.entity.MerchantBusinessInformation;
import com.merchantbusinessservice.merchantbusinessservice.repository.MerchantBusinessInformationRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class MerchantBusinessServiceTest {

    @Mock
    private MerchantBusinessInformationRepository repository;

    private MerchantBusinessService service;

    private final MerchantBusinessMapper mapper = new MerchantBusinessMapperImpl();

    @BeforeEach
    void setUp() {
        service = new MerchantBusinessService(repository, mapper, OpenTelemetry.noop());
    }

    private MerchantBusinessInformation createBusiness(Long id, Long merchantId) {
        MerchantBusinessInformation business = new MerchantBusinessInformation();
        business.setId(id);
        business.setMerchantId(merchantId);
        business.setBusinessType("PT");
        business.setTaxId("TAX-" + merchantId);
        business.setEstablishedYear(2020);
        business.setNumberOfEmployees(50);
        business.setWebsiteUrl("https://biz.example.com/" + merchantId);
        return business;
    }

    private MerchantBusinessRequest createRequest(Long merchantId) {
        return new MerchantBusinessRequest(merchantId, "CV", "NEW-TAX", 2021, 25, "https://new.example.com");
    }

    @Test
    void getAll_returnsAllFromRepository() {
        when(repository.findAll()).thenReturn(List.of(createBusiness(1L, 1L), createBusiness(2L, 2L)));

        List<MerchantBusinessInformation> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MerchantBusinessInformation::getMerchantId)
                .containsExactly(1L, 2L);
        verify(repository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNone() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.getAll()).isEmpty();
    }

    @Test
    void getByMerchantId_returnsBusinessWhenFound() {
        when(repository.findByMerchantId(1L)).thenReturn(Optional.of(createBusiness(1L, 1L)));

        MerchantBusinessInformation result = service.getByMerchantId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMerchantId()).isEqualTo(1L);
        assertThat(result.getBusinessType()).isEqualTo("PT");
    }

    @Test
    void getByMerchantId_throwsWhenNotFound() {
        when(repository.findByMerchantId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByMerchantId(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Merchant business not found");
    }

    @Test
    void getById_returnsBusinessWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(createBusiness(1L, 1L)));

        MerchantBusinessInformation result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Merchant business not found");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        MerchantBusinessRequest request = createRequest(1L);
        MerchantBusinessInformation saved = createBusiness(5L, 1L);

        when(repository.save(any(MerchantBusinessInformation.class))).thenReturn(saved);

        MerchantBusinessInformation result = service.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<MerchantBusinessInformation> captor =
                ArgumentCaptor.forClass(MerchantBusinessInformation.class);
        verify(repository).save(captor.capture());
        MerchantBusinessInformation mapped = captor.getValue();
        assertThat(mapped.getMerchantId()).isEqualTo(1L);
        assertThat(mapped.getBusinessType()).isEqualTo("CV");
        assertThat(mapped.getTaxId()).isEqualTo("NEW-TAX");
        assertThat(mapped.getEstablishedYear()).isEqualTo(2021);
        assertThat(mapped.getNumberOfEmployees()).isEqualTo(25);
        assertThat(mapped.getWebsiteUrl()).isEqualTo("https://new.example.com");
        assertThat(mapped.getId()).isNull();
    }

    @Test
    void update_updatesFieldsOnExisting() {
        MerchantBusinessInformation existing = createBusiness(1L, 1L);
        MerchantBusinessRequest request = createRequest(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(MerchantBusinessInformation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MerchantBusinessInformation result = service.update(1L, request);

        assertThat(result.getBusinessType()).isEqualTo("CV");
        assertThat(result.getTaxId()).isEqualTo("NEW-TAX");
        assertThat(result.getEstablishedYear()).isEqualTo(2021);
        assertThat(result.getNumberOfEmployees()).isEqualTo(25);
        assertThat(result.getWebsiteUrl()).isEqualTo("https://new.example.com");
        verify(repository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, createRequest(1L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Merchant business not found");

        verify(repository, never()).save(any(MerchantBusinessInformation.class));
    }

    @Test
    void delete_delegatesToRepository() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }
}
