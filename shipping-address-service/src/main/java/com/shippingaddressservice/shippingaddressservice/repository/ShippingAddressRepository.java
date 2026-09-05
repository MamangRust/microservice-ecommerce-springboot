package com.shippingaddressservice.shippingaddressservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shippingaddressservice.shippingaddressservice.entity.ShippingAddress;
import java.util.List;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    List<ShippingAddress> findByOrderId(Long orderId);
}