package com.orderitemservice.orderitemservice.repository;

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

import com.orderitemservice.orderitemservice.entity.OrderItem;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class OrderItemRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private OrderItemRepository orderItemRepository;

    private OrderItem createItem(Long orderId, Long productId, Integer quantity, Integer price) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }

    @Test
    void save_persistsItemWithGeneratedIdAndTimestamps() {
        OrderItem saved = orderItemRepository.save(createItem(100L, 5L, 2, 150000));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedItem() {
        OrderItem saved = orderItemRepository.save(createItem(100L, 5L, 2, 150000));

        Optional<OrderItem> found = orderItemRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(100L);
        assertThat(found.get().getProductId()).isEqualTo(5L);
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        Optional<OrderItem> found = orderItemRepository.findById(999999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllPersisted() {
        orderItemRepository.save(createItem(100L, 5L, 2, 150000));
        orderItemRepository.save(createItem(101L, 6L, 1, 75000));

        List<OrderItem> all = orderItemRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(OrderItem::getProductId).containsExactlyInAnyOrder(5L, 6L);
    }

    @Test
    void findByOrderId_returnsOnlyThatOrder() {
        orderItemRepository.save(createItem(100L, 5L, 2, 150000));
        orderItemRepository.save(createItem(101L, 6L, 1, 75000));

        List<OrderItem> result = orderItemRepository.findByOrderId(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(5L);
        assertThat(result.get(0).getOrderId()).isEqualTo(100L);
    }

    @Test
    void findByOrderId_returnsEmptyWhenNoMatch() {
        orderItemRepository.save(createItem(100L, 5L, 2, 150000));

        List<OrderItem> result = orderItemRepository.findByOrderId(42L);

        assertThat(result).isEmpty();
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        OrderItem saved = orderItemRepository.save(createItem(100L, 5L, 2, 150000));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setQuantity(5);
        OrderItem updated = orderItemRepository.saveAndFlush(saved);

        assertThat(updated.getQuantity()).isEqualTo(5);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        OrderItem saved = orderItemRepository.save(createItem(100L, 5L, 2, 150000));

        orderItemRepository.deleteById(saved.getId());
        orderItemRepository.flush();

        assertThat(orderItemRepository.findById(saved.getId())).isEmpty();
    }
}
