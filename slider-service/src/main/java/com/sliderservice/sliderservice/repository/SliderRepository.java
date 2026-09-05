package com.sliderservice.sliderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sliderservice.sliderservice.entity.Slider;

public interface SliderRepository extends JpaRepository<Slider, Long> {}