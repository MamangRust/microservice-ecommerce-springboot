package com.merchantbusinessservice.merchantbusinessservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "merchant_business_information")
public class MerchantBusinessInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "established_year")
    private Integer establishedYear;

    @Column(name = "number_of_employees")
    private Integer numberOfEmployees;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}