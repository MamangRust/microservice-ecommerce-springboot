package com.orderitemservice.orderitemservice.dto;

import com.orderitemservice.orderitemservice.entity.OrderItem;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:00:03+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class OrderItemMapperImpl implements OrderItemMapper {

    @Override
    public OrderItem toEntity(OrderItemRequest request) {
        if ( request == null ) {
            return null;
        }

        OrderItem orderItem = new OrderItem();

        orderItem.setOrderId( request.orderId() );
        orderItem.setProductId( request.productId() );
        orderItem.setQuantity( request.quantity() );
        orderItem.setPrice( request.price() );

        return orderItem;
    }

    @Override
    public OrderItemResponse toResponse(OrderItem entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long orderId = null;
        Long productId = null;
        Integer quantity = null;
        Integer price = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        orderId = entity.getOrderId();
        productId = entity.getProductId();
        quantity = entity.getQuantity();
        price = entity.getPrice();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        OrderItemResponse orderItemResponse = new OrderItemResponse( id, orderId, productId, quantity, price, createdAt, updatedAt );

        return orderItemResponse;
    }
}
