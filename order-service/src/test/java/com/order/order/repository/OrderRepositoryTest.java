package com.order.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.order.order.entity.Order;
import com.order.order.entity.OrderOutbox;
import com.order.order.entity.OutboxStatus;
import com.order.order.entity.PaymentStatusEnum;

@DataJpaTest(properties = {
        // no order_outbox migration exists (only V1__create_table_order.sql); ddl-auto=update
        // lets Hibernate create the outbox table while the orders table still comes from Flyway
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderOutboxRepository orderOutboxRepository;

    private Order order(UUID productId, UUID userId, Integer quantity, PaymentStatusEnum status) {
        Order order = new Order();
        order.setProductId(productId);
        order.setUserId(userId);
        order.setQuantity(quantity);
        order.setPaymentStatus(status);
        return order;
    }

    // ---- OrderRepository ----

    @Test
    void save_persistsOrderWithGeneratedUuid() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Order saved = orderRepository.save(order(productId, userId, 3, PaymentStatusEnum.PENDING));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatusEnum.PENDING);

        Order found = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getProductId()).isEqualTo(productId);
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getQuantity()).isEqualTo(3);
    }

    @Test
    void save_persistsAllPaymentStatusEnumValuesAsString() {
        Order pending = orderRepository.save(order(UUID.randomUUID(), UUID.randomUUID(), 1, PaymentStatusEnum.PENDING));
        Order completed = orderRepository.save(order(UUID.randomUUID(), UUID.randomUUID(), 2, PaymentStatusEnum.COMPLETED));
        Order refunded = orderRepository.save(order(UUID.randomUUID(), UUID.randomUUID(), 3, PaymentStatusEnum.REFUNDED));

        assertThat(orderRepository.findById(pending.getId()).orElseThrow().getPaymentStatus())
                .isEqualTo(PaymentStatusEnum.PENDING);
        assertThat(orderRepository.findById(completed.getId()).orElseThrow().getPaymentStatus())
                .isEqualTo(PaymentStatusEnum.COMPLETED);
        assertThat(orderRepository.findById(refunded.getId()).orElseThrow().getPaymentStatus())
                .isEqualTo(PaymentStatusEnum.REFUNDED);
    }

    @Test
    void findByUserId_returnsOnlyThatUsersOrders() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        orderRepository.save(order(UUID.randomUUID(), userId, 1, PaymentStatusEnum.PENDING));
        orderRepository.save(order(UUID.randomUUID(), userId, 2, PaymentStatusEnum.COMPLETED));
        orderRepository.save(order(UUID.randomUUID(), otherUserId, 3, PaymentStatusEnum.PENDING));

        List<Order> result = orderRepository.findByUserId(userId);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(o -> assertThat(o.getUserId()).isEqualTo(userId));
    }

    @Test
    void findByUserId_returnsEmptyWhenUserHasNoOrders() {
        orderRepository.save(order(UUID.randomUUID(), UUID.randomUUID(), 1, PaymentStatusEnum.PENDING));

        List<Order> result = orderRepository.findByUserId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void update_changesPaymentStatusInPlace() {
        Order saved = orderRepository.save(order(UUID.randomUUID(), UUID.randomUUID(), 1, PaymentStatusEnum.PENDING));

        saved.setPaymentStatus(PaymentStatusEnum.COMPLETED);
        Order updated = orderRepository.saveAndFlush(saved);

        assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatusEnum.COMPLETED);
        assertThat(orderRepository.findById(saved.getId()).orElseThrow().getPaymentStatus())
                .isEqualTo(PaymentStatusEnum.COMPLETED);
    }

    @Test
    void deleteById_removesOrder() {
        Order saved = orderRepository.save(order(UUID.randomUUID(), UUID.randomUUID(), 1, PaymentStatusEnum.PENDING));

        orderRepository.deleteById(saved.getId());
        orderRepository.flush();

        assertThat(orderRepository.findById(saved.getId())).isEmpty();
    }

    // ---- OrderOutboxRepository ----

    private OrderOutbox outbox(String eventId) {
        OrderOutbox outbox = new OrderOutbox();
        outbox.setAggregateType("Order");
        outbox.setAggregateId(UUID.randomUUID().toString());
        outbox.setTopic("stats.ecommerce.order.event");
        outbox.setPayload("{\"eventType\":\"order.created\"}");
        outbox.setDomain("order");
        outbox.setEventId(eventId);
        return outbox;
    }

    @Test
    void outbox_save_appliesDefaultsAndPrePersistCreatedAt() {
        OrderOutbox saved = orderOutboxRepository.save(outbox(UUID.randomUUID().toString()));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getAttempts()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getProcessedAt()).isNull();
        assertThat(saved.getLastError()).isNull();
    }

    @Test
    void outbox_findByStatusOrderByCreatedAt_returnsPendingSortedOldestFirst() {
        OrderOutbox older = orderOutboxRepository.save(outbox(UUID.randomUUID().toString()));
        OrderOutbox newer = orderOutboxRepository.save(outbox(UUID.randomUUID().toString()));

        List<OrderOutbox> pending = orderOutboxRepository.findByStatusOrderByCreatedAt(OutboxStatus.PENDING);

        assertThat(pending).extracting(OrderOutbox::getEventId)
                .containsExactly(older.getEventId(), newer.getEventId());

        newer.setStatus(OutboxStatus.PROCESSED);
        orderOutboxRepository.saveAndFlush(newer);

        assertThat(orderOutboxRepository.findByStatusOrderByCreatedAt(OutboxStatus.PENDING))
                .extracting(OrderOutbox::getEventId)
                .containsExactly(older.getEventId());
        assertThat(orderOutboxRepository.findByStatusOrderByCreatedAt(OutboxStatus.PROCESSED))
                .extracting(OrderOutbox::getEventId)
                .containsExactly(newer.getEventId());
        assertThat(orderOutboxRepository.findByStatusOrderByCreatedAt(OutboxStatus.FAILED)).isEmpty();
    }
}
