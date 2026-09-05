package com.bannerservice.bannerservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bannerservice.bannerservice.entity.Banner;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrue();
}