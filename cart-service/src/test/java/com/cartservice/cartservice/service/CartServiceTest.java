package com.cartservice.cartservice.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.cartservice.cartservice.dto.CartMapper;
import com.cartservice.cartservice.dto.CartMapperImpl;
import com.cartservice.cartservice.dto.CartRequest;
import com.cartservice.cartservice.entity.Cart;
import com.cartservice.cartservice.repository.CartRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock
    private CartRepository cartRepository;

    private CartService cartService;

    private final CartMapper cartMapper = new CartMapperImpl();

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, cartMapper, OpenTelemetry.noop());
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

    private CartRequest createRequest(Long userId, Long productId, Integer quantity) {
        return new CartRequest(userId, productId, "Product" + productId, 10_000,
                "https://cdn.example.com/p" + productId + ".png", quantity, 500);
    }

    @Test
    void getByUserId_returnsPagedResultFromRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Cart> items = List.of(createCart(1L, USER_ID, 11L), createCart(2L, USER_ID, 12L));

        when(cartRepository.findByUserId(USER_ID, pageable)).thenReturn(new PageImpl<>(items, pageable, 2));

        Page<Cart> result = cartService.getByUserId(USER_ID, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Cart::getProductId).containsExactly(11L, 12L);
        verify(cartRepository).findByUserId(USER_ID, pageable);
    }

    @Test
    void getByUserId_returnsEmptyPageWhenCartIsEmpty() {
        Pageable pageable = PageRequest.of(0, 20);

        when(cartRepository.findByUserId(USER_ID, pageable)).thenReturn(Page.empty(pageable));

        Page<Cart> result = cartService.getByUserId(USER_ID, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void addItem_mapsRequestToEntityAndSaves() {
        CartRequest request = createRequest(USER_ID, 11L, 3);
        Cart saved = createCart(5L, USER_ID, 11L);

        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        Cart result = cartService.addItem(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getProductId()).isEqualTo(11L);
        assertThat(captor.getValue().getName()).isEqualTo("Product11");
        assertThat(captor.getValue().getPrice()).isEqualTo(10_000);
        assertThat(captor.getValue().getQuantity()).isEqualTo(3);
        assertThat(captor.getValue().getWeight()).isEqualTo(500);
    }

    @Test
    void removeItem_deletesWhenOwnerMatches() {
        Cart cart = createCart(1L, USER_ID, 11L);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        cartService.removeItem(1L, USER_ID);

        verify(cartRepository).delete(cart);
    }

    @Test
    void removeItem_throwsWhenCartItemNotFound() {
        when(cartRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(999L, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart item not found");

        verify(cartRepository, never()).delete(any(Cart.class));
    }

    @Test
    void removeItem_throwsUnauthorizedWhenUserMismatch() {
        Cart cart = createCart(1L, USER_ID, 11L);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.removeItem(1L, OTHER_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unauthorized");

        verify(cartRepository, never()).delete(any(Cart.class));
    }

    @Test
    void clearCart_delegatesToDeleteByUserIdAndIdIn() {
        List<Long> cartIds = List.of(1L, 2L, 3L);

        cartService.clearCart(USER_ID, cartIds);

        verify(cartRepository).deleteByUserIdAndIdIn(USER_ID, cartIds);
    }
}
