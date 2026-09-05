package com.shippingaddressservice.shippingaddressservice.service;

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

import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressMapper;
import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressMapperImpl;
import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressRequest;
import com.shippingaddressservice.shippingaddressservice.entity.ShippingAddress;
import com.shippingaddressservice.shippingaddressservice.repository.ShippingAddressRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class ShippingAddressServiceTest {

    @Mock
    private ShippingAddressRepository shippingAddressRepository;

    private ShippingAddressService shippingAddressService;

    private final ShippingAddressMapper shippingAddressMapper = new ShippingAddressMapperImpl();

    @BeforeEach
    void setUp() {
        shippingAddressService = new ShippingAddressService(shippingAddressRepository, shippingAddressMapper,
                OpenTelemetry.noop());
    }

    private ShippingAddress createAddress(Long id, Long orderId) {
        ShippingAddress address = new ShippingAddress();
        address.setId(id);
        address.setOrderId(orderId);
        address.setAlamat("Jl. Merdeka No. 10");
        address.setProvinsi("Jawa Barat");
        address.setNegara("Indonesia");
        address.setKota("Bandung");
        address.setCourier("jne");
        address.setShippingMethod("REG");
        address.setShippingCost(20_000);
        return address;
    }

    private ShippingAddressRequest createRequest(Long orderId) {
        return new ShippingAddressRequest(orderId, "Jl. Sukajadi No. 5", "Jawa Barat", "Indonesia",
                "Bandung", "jne", "REG", 25_000);
    }

    @Test
    void getAll_returnsAllFromRepository() {
        when(shippingAddressRepository.findAll())
                .thenReturn(List.of(createAddress(1L, 100L), createAddress(2L, 101L)));

        List<ShippingAddress> result = shippingAddressService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ShippingAddress::getOrderId).containsExactly(100L, 101L);
        verify(shippingAddressRepository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNoData() {
        when(shippingAddressRepository.findAll()).thenReturn(List.of());

        assertThat(shippingAddressService.getAll()).isEmpty();
    }

    @Test
    void getByOrderId_returnsFromRepository() {
        when(shippingAddressRepository.findByOrderId(100L))
                .thenReturn(List.of(createAddress(1L, 100L)));

        List<ShippingAddress> result = shippingAddressService.getByOrderId(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(100L);
        verify(shippingAddressRepository).findByOrderId(100L);
    }

    @Test
    void getByOrderId_returnsEmptyWhenNoMatch() {
        when(shippingAddressRepository.findByOrderId(42L)).thenReturn(List.of());

        assertThat(shippingAddressService.getByOrderId(42L)).isEmpty();
    }

    @Test
    void getById_returnsAddressWhenFound() {
        when(shippingAddressRepository.findById(1L)).thenReturn(Optional.of(createAddress(1L, 100L)));

        ShippingAddress result = shippingAddressService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getKota()).isEqualTo("Bandung");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(shippingAddressRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingAddressService.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Address not found");

        verify(shippingAddressRepository, never()).save(any(ShippingAddress.class));
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        ShippingAddressRequest request = createRequest(100L);
        ShippingAddress saved = createAddress(5L, 100L);

        when(shippingAddressRepository.save(any(ShippingAddress.class))).thenReturn(saved);

        ShippingAddress result = shippingAddressService.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<ShippingAddress> captor = ArgumentCaptor.forClass(ShippingAddress.class);
        verify(shippingAddressRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(100L);
        assertThat(captor.getValue().getAlamat()).isEqualTo("Jl. Sukajadi No. 5");
        assertThat(captor.getValue().getProvinsi()).isEqualTo("Jawa Barat");
        assertThat(captor.getValue().getNegara()).isEqualTo("Indonesia");
        assertThat(captor.getValue().getKota()).isEqualTo("Bandung");
        assertThat(captor.getValue().getCourier()).isEqualTo("jne");
        assertThat(captor.getValue().getShippingMethod()).isEqualTo("REG");
        assertThat(captor.getValue().getShippingCost()).isEqualTo(25_000);
    }

    @Test
    void update_updatesAllFieldsOnExisting() {
        ShippingAddress existing = createAddress(1L, 100L);
        ShippingAddressRequest request = createRequest(100L);

        when(shippingAddressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(shippingAddressRepository.save(any(ShippingAddress.class))).thenAnswer(inv -> inv.getArgument(0));

        ShippingAddress result = shippingAddressService.update(1L, request);

        assertThat(result.getAlamat()).isEqualTo("Jl. Sukajadi No. 5");
        assertThat(result.getProvinsi()).isEqualTo("Jawa Barat");
        assertThat(result.getNegara()).isEqualTo("Indonesia");
        assertThat(result.getKota()).isEqualTo("Bandung");
        assertThat(result.getCourier()).isEqualTo("jne");
        assertThat(result.getShippingMethod()).isEqualTo("REG");
        assertThat(result.getShippingCost()).isEqualTo(25_000);
        verify(shippingAddressRepository).save(existing);
    }

    @Test
    void update_keepsOrderIdUnchanged() {
        ShippingAddress existing = createAddress(1L, 100L);
        ShippingAddressRequest request = createRequest(999L);

        when(shippingAddressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(shippingAddressRepository.save(any(ShippingAddress.class))).thenAnswer(inv -> inv.getArgument(0));

        ShippingAddress result = shippingAddressService.update(1L, request);

        assertThat(result.getOrderId()).isEqualTo(100L);
        verify(shippingAddressRepository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(shippingAddressRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingAddressService.update(999L, createRequest(100L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Address not found");

        verify(shippingAddressRepository, never()).save(any(ShippingAddress.class));
    }

    @Test
    void delete_deletesById() {
        shippingAddressService.delete(1L);

        verify(shippingAddressRepository).deleteById(1L);
    }
}
