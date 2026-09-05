package com.shippingaddressservice.shippingaddressservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shipping_addresses")
public class ShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "alamat", columnDefinition = "TEXT")
    private String alamat;

    @Column(name = "provinsi", length = 255)
    private String provinsi;

    @Column(name = "negara", length = 255)
    private String negara;

    @Column(name = "kota", length = 255)
    private String kota;

    @Column(name = "courier", length = 100)
    private String courier;

    @Column(name = "shipping_method", length = 255)
    private String shippingMethod;

    @Column(name = "shipping_cost")
    private Integer shippingCost;

    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}