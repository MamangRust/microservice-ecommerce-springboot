package com.cartservice.cartservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cartservice.cartservice.entity.Cart;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Page<Cart> findByUserId(Long userId, Pageable pageable);
    List<Cart> findByUserId(Long userId);
    void deleteByUserIdAndIdIn(Long userId, List<Long> cartIds);
}