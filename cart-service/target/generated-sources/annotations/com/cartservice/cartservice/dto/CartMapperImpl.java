package com.cartservice.cartservice.dto;

import com.cartservice.cartservice.entity.Cart;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T12:56:28+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class CartMapperImpl implements CartMapper {

    @Override
    public Cart toEntity(CartRequest request) {
        if ( request == null ) {
            return null;
        }

        Cart cart = new Cart();

        cart.setUserId( request.userId() );
        cart.setProductId( request.productId() );
        cart.setName( request.name() );
        cart.setPrice( request.price() );
        cart.setImage( request.image() );
        cart.setQuantity( request.quantity() );
        cart.setWeight( request.weight() );

        return cart;
    }

    @Override
    public CartResponse toResponse(Cart entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long userId = null;
        Long productId = null;
        String name = null;
        Integer price = null;
        String image = null;
        Integer quantity = null;
        Integer weight = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        userId = entity.getUserId();
        productId = entity.getProductId();
        name = entity.getName();
        price = entity.getPrice();
        image = entity.getImage();
        quantity = entity.getQuantity();
        weight = entity.getWeight();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        CartResponse cartResponse = new CartResponse( id, userId, productId, name, price, image, quantity, weight, createdAt, updatedAt );

        return cartResponse;
    }
}
