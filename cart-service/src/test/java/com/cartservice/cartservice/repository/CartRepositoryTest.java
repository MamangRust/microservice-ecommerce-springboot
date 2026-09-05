package com.cartservice.cartservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.cartservice.cartservice.entity.Cart;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class CartRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private CartRepository cartRepository;

    private Cart createCart(Long userId, Long productId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setName("Product" + productId);
        cart.setPrice(10_000);
        cart.setQuantity(1);
        cart.setWeight(500);
        return cart;
    }

    @Test
    void save_persistsCartWithGeneratedIdAndTimestamps() {
        Cart saved = cartRepository.save(createCart(1L, 11L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findByUserId_paged_returnsRequestedSliceWithTotal() {
        for (long productId = 11L; productId <= 13L; productId++) {
            cartRepository.save(createCart(1L, productId));
        }

        Page<Cart> page = cartRepository.findByUserId(1L, PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Cart::getUserId).containsOnly(1L);
    }

    @Test
    void findByUserId_paged_returnsSecondPage() {
        for (long productId = 11L; productId <= 13L; productId++) {
            cartRepository.save(createCart(1L, productId));
        }

        Page<Cart> page = cartRepository.findByUserId(1L, PageRequest.of(1, 2));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    void findByUserId_paged_returnsEmptyForUnknownUser() {
        Page<Cart> page = cartRepository.findByUserId(42L, PageRequest.of(0, 20));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void findByUserId_unpaginated_returnsOnlyThatUsersItems() {
        cartRepository.save(createCart(1L, 11L));
        cartRepository.save(createCart(1L, 12L));
        cartRepository.save(createCart(1L, 13L));
        cartRepository.save(createCart(2L, 11L));

        List<Cart> user1 = cartRepository.findByUserId(1L);
        List<Cart> user2 = cartRepository.findByUserId(2L);

        assertThat(user1).hasSize(3);
        assertThat(user1).extracting(Cart::getUserId).containsOnly(1L);
        assertThat(user2).hasSize(1);
    }

    @Test
    void findByUserId_unpaginated_returnsEmptyWhenNoItems() {
        assertThat(cartRepository.findByUserId(42L)).isEmpty();
    }

    @Test
    void deleteByUserIdAndIdIn_deletesOnlyMatchingUserAndIds() {
        Cart user1ItemA = cartRepository.save(createCart(1L, 11L));
        Cart user1ItemB = cartRepository.save(createCart(1L, 12L));
        Cart user2Item = cartRepository.save(createCart(2L, 11L));

        // Quirk: derived delete tanpa @Transactional di service; di sini aman
        // karena @DataJpaTest membungkus test dalam satu transaction.
        cartRepository.deleteByUserIdAndIdIn(1L, List.of(user1ItemA.getId(), user1ItemB.getId()));
        cartRepository.flush();

        assertThat(cartRepository.findById(user1ItemA.getId())).isEmpty();
        assertThat(cartRepository.findById(user1ItemB.getId())).isEmpty();
        assertThat(cartRepository.findById(user2Item.getId())).isPresent();
    }

    @Test
    void deleteByUserIdAndIdIn_leavesRowsUntouchedWhenNoMatch() {
        Cart user1Item = cartRepository.save(createCart(1L, 11L));
        Cart user2Item = cartRepository.save(createCart(2L, 11L));

        cartRepository.deleteByUserIdAndIdIn(1L, List.of(user2Item.getId()));
        cartRepository.flush();

        assertThat(cartRepository.findById(user1Item.getId())).isPresent();
        assertThat(cartRepository.findById(user2Item.getId())).isPresent();
    }
}
