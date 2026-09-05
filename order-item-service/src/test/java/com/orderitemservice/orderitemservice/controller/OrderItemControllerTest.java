package com.orderitemservice.orderitemservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orderitemservice.orderitemservice.dto.OrderItemMapper;
import com.orderitemservice.orderitemservice.dto.OrderItemMapperImpl;
import com.orderitemservice.orderitemservice.dto.OrderItemRequest;
import com.orderitemservice.orderitemservice.entity.OrderItem;
import com.orderitemservice.orderitemservice.exc.GeneralExceptionHandler;
import com.orderitemservice.orderitemservice.service.OrderItemService;

@ExtendWith(MockitoExtension.class)
class OrderItemControllerTest {

    @Mock
    private OrderItemService orderItemService;

    private MockMvc mockMvc;

    private final OrderItemMapper orderItemMapper = new OrderItemMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        OrderItemController controller = new OrderItemController(orderItemService, orderItemMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
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

    @Test
    void getAllOrderItems_returnsMappedList() throws Exception {
        when(orderItemService.getAll()).thenReturn(List.of(createItem(1L, 100L, 5L, 2, 150000)));

        mockMvc.perform(get("/order-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].price").value(150000));
    }

    @Test
    void getAllOrderItems_returnsEmptyListWhenNone() throws Exception {
        when(orderItemService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/order-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getItemsByOrder_returnsMappedList() throws Exception {
        when(orderItemService.getByOrderId(100L))
                .thenReturn(List.of(createItem(1L, 100L, 5L, 2, 150000)));

        mockMvc.perform(get("/order-items/order/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(100))
                .andExpect(jsonPath("$[0].productId").value(5));
    }

    @Test
    void getOrderItemById_returnsResponse() throws Exception {
        when(orderItemService.getById(1L)).thenReturn(createItem(1L, 100L, 5L, 2, 150000));

        mockMvc.perform(get("/order-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void getOrderItemById_returns404WhenNotFound() throws Exception {
        when(orderItemService.getById(99L)).thenThrow(new RuntimeException("Order item not found"));

        mockMvc.perform(get("/order-items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Order item not found"));
    }

    @Test
    void createOrderItem_returnsResponse() throws Exception {
        OrderItemRequest request = new OrderItemRequest(100L, 5L, 2, 150000);

        when(orderItemService.create(any(OrderItemRequest.class)))
                .thenReturn(createItem(5L, 100L, 5L, 2, 150000));

        mockMvc.perform(post("/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void createOrderItem_returns400WhenQuantityZero() throws Exception {
        String body = "{\"orderId\": 100, \"productId\": 5, \"quantity\": 0, \"price\": 100}";

        mockMvc.perform(post("/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(orderItemService, never()).create(any(OrderItemRequest.class));
    }

    @Test
    void createOrderItem_returns400WhenPriceNegative() throws Exception {
        String body = "{\"orderId\": 100, \"productId\": 5, \"quantity\": 1, \"price\": -1}";

        mockMvc.perform(post("/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(orderItemService, never()).create(any(OrderItemRequest.class));
    }

    @Test
    void createOrderItem_returns400WhenOrderIdNull() throws Exception {
        String body = "{\"orderId\": null, \"productId\": 5, \"quantity\": 1, \"price\": 100}";

        mockMvc.perform(post("/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(orderItemService, never()).create(any(OrderItemRequest.class));
    }

    @Test
    void updateOrderItem_returnsUpdatedResponse() throws Exception {
        OrderItemRequest request = new OrderItemRequest(100L, 5L, 4, 140000);

        when(orderItemService.update(eq(1L), any(OrderItemRequest.class)))
                .thenReturn(createItem(1L, 100L, 5L, 4, 140000));

        mockMvc.perform(put("/order-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4))
                .andExpect(jsonPath("$.price").value(140000));
    }

    @Test
    void updateOrderItem_returns404WhenNotFound() throws Exception {
        OrderItemRequest request = new OrderItemRequest(100L, 5L, 4, 140000);

        when(orderItemService.update(eq(99L), any(OrderItemRequest.class)))
                .thenThrow(new RuntimeException("Order item not found"));

        mockMvc.perform(put("/order-items/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrderItem_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/order-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Order item deleted"));

        verify(orderItemService).delete(1L);
    }

    @Test
    void deleteOrderItem_returns404WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Order item not found")).when(orderItemService).delete(99L);

        mockMvc.perform(delete("/order-items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Order item not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        OrderItem item = createItem(1L, 100L, 5L, 2, 150000);
        item.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));

        when(orderItemService.getById(1L)).thenReturn(item);

        mockMvc.perform(get("/order-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026));
    }
}
