package com.sliderservice.sliderservice.dto;

import com.sliderservice.sliderservice.entity.Slider;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T12:54:52+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class SliderMapperImpl implements SliderMapper {

    @Override
    public Slider toEntity(SliderRequest request) {
        if ( request == null ) {
            return null;
        }

        Slider slider = new Slider();

        slider.setName( request.name() );
        slider.setImage( request.image() );

        return slider;
    }

    @Override
    public SliderResponse toResponse(Slider entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String image = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        name = entity.getName();
        image = entity.getImage();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        SliderResponse sliderResponse = new SliderResponse( id, name, image, createdAt, updatedAt );

        return sliderResponse;
    }
}
