package com.sliderservice.sliderservice.service;

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

import com.sliderservice.sliderservice.dto.SliderMapper;
import com.sliderservice.sliderservice.dto.SliderMapperImpl;
import com.sliderservice.sliderservice.dto.SliderRequest;
import com.sliderservice.sliderservice.entity.Slider;
import com.sliderservice.sliderservice.repository.SliderRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class SliderServiceTest {

    @Mock
    private SliderRepository sliderRepository;

    private SliderService sliderService;

    private final SliderMapper sliderMapper = new SliderMapperImpl();

    @BeforeEach
    void setUp() {
        sliderService = new SliderService(sliderRepository, sliderMapper, OpenTelemetry.noop());
    }

    private Slider createSlider(Long id, String name) {
        Slider slider = new Slider();
        slider.setId(id);
        slider.setName(name);
        slider.setImage("https://cdn.example.com/" + name + ".png");
        return slider;
    }

    private SliderRequest createRequest(String name, String image) {
        return new SliderRequest(name, image);
    }

    @Test
    void getAll_returnsAllFromRepository() {
        when(sliderRepository.findAll())
                .thenReturn(List.of(createSlider(1L, "Slider1"), createSlider(2L, "Slider2")));

        List<Slider> result = sliderService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Slider::getName).containsExactly("Slider1", "Slider2");
        verify(sliderRepository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNoData() {
        when(sliderRepository.findAll()).thenReturn(List.of());

        assertThat(sliderService.getAll()).isEmpty();
    }

    @Test
    void getById_returnsSliderWhenFound() {
        when(sliderRepository.findById(1L)).thenReturn(Optional.of(createSlider(1L, "Slider1")));

        Slider result = sliderService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Slider1");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(sliderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sliderService.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Slider not found");

        verify(sliderRepository, never()).save(any(Slider.class));
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        SliderRequest request = createRequest("NewSlider", "https://cdn.example.com/new.png");
        Slider saved = createSlider(5L, "NewSlider");

        when(sliderRepository.save(any(Slider.class))).thenReturn(saved);

        Slider result = sliderService.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<Slider> captor = ArgumentCaptor.forClass(Slider.class);
        verify(sliderRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("NewSlider");
        assertThat(captor.getValue().getImage()).isEqualTo("https://cdn.example.com/new.png");
    }

    @Test
    void update_updatesNameAndImage() {
        Slider existing = createSlider(1L, "OldSlider");

        when(sliderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sliderRepository.save(any(Slider.class))).thenAnswer(inv -> inv.getArgument(0));

        Slider result = sliderService.update(1L, createRequest("UpdatedSlider", "https://cdn.example.com/updated.png"));

        assertThat(result.getName()).isEqualTo("UpdatedSlider");
        assertThat(result.getImage()).isEqualTo("https://cdn.example.com/updated.png");
        verify(sliderRepository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(sliderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sliderService.update(999L, createRequest("X", "img.png")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Slider not found");

        verify(sliderRepository, never()).save(any(Slider.class));
    }

    @Test
    void delete_deletesById() {
        sliderService.delete(1L);

        verify(sliderRepository).deleteById(1L);
    }
}
