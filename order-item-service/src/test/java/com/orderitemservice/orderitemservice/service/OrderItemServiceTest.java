package com.orderitemservice.orderitemservice.service;

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

import com.orderitemservice.orderitemservice.dto.OrderItemMapper;
import com.orderitemservice.orderitemservice.dto.OrderItemMapperImpl;
import com.orderitemservice.orderitemservice.dto.OrderItemRequest;
import com.orderitemservice.orderitemservice.entity.OrderItem;
import com.orderitemservice.orderitemservice.repository.OrderItemRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    private OrderItemService orderItemService;

    private final OrderItemMapper orderItemMapper = new OrderItemMapperImpl();

    @BeforeEach
    void setUp() {
        orderItemService = new OrderItemService(orderItemRepository, orderItemMapper, OpenTelemetry.noop());
    }

    private OrderItem createItem(Long id, Long orderId, Long productId, Integer quantity, Integer price) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }

    private OrderItemRequest createRequest(Long orderId, Long productId, Integer quantity, Integer price) {
        return new OrderItemRequest(orderId, productId, quantity, price);
    }

    @Test
    void getAll_returnsAllFromRepository() {
        OrderItem i1 = createItem(1L, 100L, 5L, 2, 150000);
        OrderItem i2 = createItem(2L, 100L, 6L, 1, 75000);

        when(orderItemRepository.findAll()).thenReturn(List.of(i1, i2));

        List<OrderItem> result = orderItemService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderItem::getProductId).containsExactly(5L, 6L);
        verify(orderItemRepository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNoItems() {
        when(orderItemRepository.findAll()).thenReturn(List.of());

        List<OrderItem> result = orderItemService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getByOrderId_returnsFromRepository() {
        when(orderItemRepository.findByOrderId(100L))
                .thenReturn(List.of(createItem(1L, 100L, 5L, 2, 150000)));

        List<OrderItem> result = orderItemService.getByOrderId(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(100L);
        verify(orderItemRepository).findByOrderId(100L);
    }

    @Test
    void getByOrderId_returnsEmptyWhenNoMatch() {
        when(orderItemRepository.findByOrderId(42L)).thenReturn(List.of());

        List<OrderItem> result = orderItemService.getByOrderId(42L);

        assertThat(result).isEmpty();
    }

    @Test
    void getById_returnsItemWhenFound() {
        when(orderItemRepository.findById(1L))
                .thenReturn(Optional.of(createItem(1L, 100L, 5L, 2, 150000)));

        OrderItem result = orderItemService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getQuantity()).isEqualTo(2);
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(orderItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order item not found");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        OrderItemRequest request = createRequest(100L, 5L, 2, 150000);
        OrderItem saved = createItem(5L, 100L, 5L, 2, 150000);

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(saved);

        OrderItem result = orderItemService.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(100L);
        assertThat(captor.getValue().getProductId()).isEqualTo(5L);
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);
        assertThat(captor.getValue().getPrice()).isEqualTo(150000);
    }

    @Test
    void update_updatesFieldsOnExisting() {
        OrderItem existing = createItem(1L, 100L, 5L, 2, 150000);
        OrderItemRequest request = createRequest(100L, 5L, 4, 140000);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderItem result = orderItemService.update(1L, request);

        assertThat(result.getQuantity()).isEqualTo(4);
        assertThat(result.getPrice()).isEqualTo(140000);
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getProductId()).isEqualTo(5L);
        verify(orderItemRepository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(orderItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.update(999L, createRequest(100L, 5L, 1, 1000)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order item not found");

        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void delete_delegatesToRepository() {
        orderItemService.delete(1L);

        verify(orderItemRepository).deleteById(1L);
    }
}
