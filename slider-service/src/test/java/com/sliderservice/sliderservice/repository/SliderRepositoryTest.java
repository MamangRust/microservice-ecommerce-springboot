package com.sliderservice.sliderservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.sliderservice.sliderservice.entity.Slider;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class SliderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private SliderRepository sliderRepository;

    private Slider createSlider(String name) {
        Slider slider = new Slider();
        slider.setName(name);
        slider.setImage("https://cdn.example.com/" + name + ".png");
        return slider;
    }

    @Test
    void save_persistsSliderWithGeneratedIdAndTimestamps() {
        Slider saved = sliderRepository.save(createSlider("Slider1"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void findById_returnsSavedSlider() {
        Slider saved = sliderRepository.save(createSlider("Slider2"));

        Optional<Slider> found = sliderRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Slider2");
        assertThat(found.get().getImage()).isEqualTo("https://cdn.example.com/Slider2.png");
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(sliderRepository.findById(999999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllPersisted() {
        sliderRepository.save(createSlider("Slider1"));
        sliderRepository.save(createSlider("Slider2"));

        List<Slider> all = sliderRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Slider::getName).containsExactlyInAnyOrder("Slider1", "Slider2");
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        Slider saved = sliderRepository.save(createSlider("Before"));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setName("After");
        Slider updated = sliderRepository.saveAndFlush(saved);

        assertThat(updated.getName()).isEqualTo("After");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        Slider saved = sliderRepository.save(createSlider("DeleteMe"));

        sliderRepository.deleteById(saved.getId());
        sliderRepository.flush();

        assertThat(sliderRepository.findById(saved.getId())).isEmpty();
    }
}
