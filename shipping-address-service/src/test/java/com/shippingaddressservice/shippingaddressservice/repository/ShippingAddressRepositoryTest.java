package com.shippingaddressservice.shippingaddressservice.repository;

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

import com.shippingaddressservice.shippingaddressservice.entity.ShippingAddress;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ShippingAddressRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    private ShippingAddress createAddress(Long orderId) {
        ShippingAddress address = new ShippingAddress();
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

    @Test
    void save_persistsAddressWithGeneratedIdAndTimestamps() {
        ShippingAddress saved = shippingAddressRepository.save(createAddress(100L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void save_persistsAllFields() {
        ShippingAddress saved = shippingAddressRepository.save(createAddress(100L));

        ShippingAddress found = shippingAddressRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getOrderId()).isEqualTo(100L);
        assertThat(found.getAlamat()).isEqualTo("Jl. Merdeka No. 10");
        assertThat(found.getProvinsi()).isEqualTo("Jawa Barat");
        assertThat(found.getNegara()).isEqualTo("Indonesia");
        assertThat(found.getKota()).isEqualTo("Bandung");
        assertThat(found.getCourier()).isEqualTo("jne");
        assertThat(found.getShippingMethod()).isEqualTo("REG");
        assertThat(found.getShippingCost()).isEqualTo(20_000);
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(shippingAddressRepository.findById(999999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllPersisted() {
        shippingAddressRepository.save(createAddress(100L));
        shippingAddressRepository.save(createAddress(101L));

        List<ShippingAddress> all = shippingAddressRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(ShippingAddress::getOrderId).containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    void findByOrderId_returnsOnlyThatOrder() {
        shippingAddressRepository.save(createAddress(100L));
        shippingAddressRepository.save(createAddress(100L));
        shippingAddressRepository.save(createAddress(101L));

        List<ShippingAddress> result = shippingAddressRepository.findByOrderId(100L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ShippingAddress::getOrderId).containsOnly(100L);
    }

    @Test
    void findByOrderId_returnsEmptyWhenNoMatch() {
        shippingAddressRepository.save(createAddress(100L));

        assertThat(shippingAddressRepository.findByOrderId(42L)).isEmpty();
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        ShippingAddress saved = shippingAddressRepository.save(createAddress(100L));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setKota("Surabaya");
        saved.setShippingCost(30_000);
        ShippingAddress updated = shippingAddressRepository.saveAndFlush(saved);

        assertThat(updated.getKota()).isEqualTo("Surabaya");
        assertThat(updated.getShippingCost()).isEqualTo(30_000);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        ShippingAddress saved = shippingAddressRepository.save(createAddress(100L));

        shippingAddressRepository.deleteById(saved.getId());
        shippingAddressRepository.flush();

        assertThat(shippingAddressRepository.findById(saved.getId())).isEmpty();
    }
}
