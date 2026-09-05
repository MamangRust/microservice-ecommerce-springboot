package com.cartservice.cartservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cartservice.cartservice.dto.CartMapper;
import com.cartservice.cartservice.dto.CartMapperImpl;
import com.cartservice.cartservice.dto.CartRequest;
import com.cartservice.cartservice.entity.Cart;
import com.cartservice.cartservice.exc.GeneralExceptionHandler;
import com.cartservice.cartservice.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private CartService cartService;

    private MockMvc mockMvc;

    private final CartMapper cartMapper = new CartMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        CartController controller = new CartController(cartService, cartMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private Cart createCart(Long id, Long userId, Long productId) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setName("Product" + productId);
        cart.setPrice(10_000);
        cart.setQuantity(2);
        return cart;
    }

    @Test
    void getUserCart_returnsPagedResponse() throws Exception {
        Cart item1 = createCart(1L, USER_ID, 11L);
        Cart item2 = createCart(2L, USER_ID, 12L);

        when(cartService.getByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item1, item2), PageRequest.of(0, 20), 2));

        mockMvc.perform(get("/carts/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].productId").value(11))
                .andExpect(jsonPath("$.content[1].productId").value(12))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getUserCart_returnsEmptyPageWhenNoItems() throws Exception {
        when(cartService.getByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/carts/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getUserCart_passesPageAndSizeToService() throws Exception {
        when(cartService.getByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

        mockMvc.perform(get("/carts/user/{userId}", USER_ID)
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cartService).getByUserId(eq(USER_ID), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void addItem_returnsCreatedResponse() throws Exception {
        CartRequest request = new CartRequest(USER_ID, 11L, "Product11", 10_000,
                "https://cdn.example.com/p11.png", 2, 500);

        when(cartService.addItem(any(CartRequest.class))).thenReturn(createCart(5L, USER_ID, 11L));

        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.productId").value(11))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void addItem_returns500WhenServiceFails() throws Exception {
        CartRequest request = new CartRequest(USER_ID, 11L, "Product11", 10_000,
                "https://cdn.example.com/p11.png", 2, 500);

        when(cartService.addItem(any(CartRequest.class))).thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void addItem_returns400WhenQuantityZero() throws Exception {
        String body = "{\"userId\": 1, \"productId\": 11, \"quantity\": 0}";

        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(cartService, never()).addItem(any(CartRequest.class));
    }

    @Test
    void addItem_returns400WhenUserIdNull() throws Exception {
        String body = "{\"userId\": null, \"productId\": 11, \"quantity\": 1}";

        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItem(any(CartRequest.class));
    }

    @Test
    void addItem_returns400WhenProductIdNull() throws Exception {
        String body = "{\"userId\": 1, \"productId\": null, \"quantity\": 1}";

        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItem(any(CartRequest.class));
    }

    @Test
    void removeItem_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/carts/{cartId}/user/{userId}", 1L, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Item removed"));

        verify(cartService).removeItem(1L, USER_ID);
    }

    @Test
    void removeItem_returns400WhenCartItemNotFound() throws Exception {
        doThrow(new RuntimeException("Cart item not found"))
                .when(cartService).removeItem(99L, USER_ID);

        mockMvc.perform(delete("/carts/{cartId}/user/{userId}", 99L, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Cart item not found"));
    }

    @Test
    void removeItem_returns400WhenUnauthorized() throws Exception {
        doThrow(new RuntimeException("Unauthorized"))
                .when(cartService).removeItem(1L, 2L);

        mockMvc.perform(delete("/carts/{cartId}/user/{userId}", 1L, 2L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Unauthorized"));
    }

    @Test
    void clearCart_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/carts/user/{userId}", USER_ID)
                        .param("cartIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Cart cleared"));

        verify(cartService).clearCart(USER_ID, List.of(1L, 2L));
    }
}
