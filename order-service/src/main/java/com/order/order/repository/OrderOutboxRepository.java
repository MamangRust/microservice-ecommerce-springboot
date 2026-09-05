package com.order.order.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.order.order.entity.OrderOutbox;
import com.order.order.entity.OutboxStatus;

public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, Long> {
    List<OrderOutbox> findByStatusOrderByCreatedAt(OutboxStatus status);
}