package com.shippingaddressservice.shippingaddressservice.dto;

import com.shippingaddressservice.shippingaddressservice.entity.ShippingAddress;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:08:45+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class ShippingAddressMapperImpl implements ShippingAddressMapper {

    @Override
    public ShippingAddress toEntity(ShippingAddressRequest request) {
        if ( request == null ) {
            return null;
        }

        ShippingAddress shippingAddress = new ShippingAddress();

        shippingAddress.setOrderId( request.orderId() );
        shippingAddress.setAlamat( request.alamat() );
        shippingAddress.setProvinsi( request.provinsi() );
        shippingAddress.setNegara( request.negara() );
        shippingAddress.setKota( request.kota() );
        shippingAddress.setCourier( request.courier() );
        shippingAddress.setShippingMethod( request.shippingMethod() );
        shippingAddress.setShippingCost( request.shippingCost() );

        return shippingAddress;
    }

    @Override
    public ShippingAddressResponse toResponse(ShippingAddress entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        Long orderId = null;
        String alamat = null;
        String provinsi = null;
        String negara = null;
        String kota = null;
        String courier = null;
        String shippingMethod = null;
        Integer shippingCost = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        orderId = entity.getOrderId();
        alamat = entity.getAlamat();
        provinsi = entity.getProvinsi();
        negara = entity.getNegara();
        kota = entity.getKota();
        courier = entity.getCourier();
        shippingMethod = entity.getShippingMethod();
        shippingCost = entity.getShippingCost();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        ShippingAddressResponse shippingAddressResponse = new ShippingAddressResponse( id, orderId, alamat, provinsi, negara, kota, courier, shippingMethod, shippingCost, createdAt, updatedAt );

        return shippingAddressResponse;
    }
}
