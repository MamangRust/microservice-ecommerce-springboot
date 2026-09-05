package com.sliderservice.sliderservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.sliderservice.sliderservice.dto.SliderMapper;
import com.sliderservice.sliderservice.dto.SliderMapperImpl;
import com.sliderservice.sliderservice.dto.SliderRequest;
import com.sliderservice.sliderservice.entity.Slider;
import com.sliderservice.sliderservice.exc.GeneralExceptionHandler;
import com.sliderservice.sliderservice.service.SliderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class SliderControllerTest {

    @Mock
    private SliderService sliderService;

    private MockMvc mockMvc;

    private final SliderMapper sliderMapper = new SliderMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        SliderController controller = new SliderController(sliderService, sliderMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private Slider createSlider(Long id, String name) {
        Slider slider = new Slider();
        slider.setId(id);
        slider.setName(name);
        slider.setImage("https://cdn.example.com/" + name + ".png");
        return slider;
    }

    @Test
    void getAll_returnsMappedList() throws Exception {
        when(sliderService.getAll()).thenReturn(List.of(createSlider(1L, "Slider1")));

        mockMvc.perform(get("/sliders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Slider1"));
    }

    @Test
    void getAll_returnsEmptyListWhenNone() throws Exception {
        when(sliderService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/sliders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_returnsResponse() throws Exception {
        when(sliderService.getById(1L)).thenReturn(createSlider(1L, "Slider1"));

        mockMvc.perform(get("/sliders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.image").value("https://cdn.example.com/Slider1.png"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(sliderService.getById(99L)).thenThrow(new RuntimeException("Slider not found"));

        mockMvc.perform(get("/sliders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Slider not found"));
    }

    @Test
    void create_returnsCreatedResponse() throws Exception {
        SliderRequest request = new SliderRequest("NewSlider", "https://cdn.example.com/new.png");

        when(sliderService.create(any(SliderRequest.class))).thenReturn(createSlider(5L, "NewSlider"));

        mockMvc.perform(post("/sliders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("NewSlider"));
    }

    @Test
    void create_returns500WhenServiceFails() throws Exception {
        SliderRequest request = new SliderRequest("NewSlider", "https://cdn.example.com/new.png");

        when(sliderService.create(any(SliderRequest.class))).thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/sliders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void create_returns400WhenNameBlank() throws Exception {
        SliderRequest request = new SliderRequest(" ", "https://cdn.example.com/new.png");

        mockMvc.perform(post("/sliders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(sliderService, never()).create(any(SliderRequest.class));
    }

    @Test
    void update_returnsUpdatedResponse() throws Exception {
        SliderRequest request = new SliderRequest("UpdatedSlider", "https://cdn.example.com/updated.png");

        when(sliderService.update(eq(1L), any(SliderRequest.class)))
                .thenReturn(createSlider(1L, "UpdatedSlider"));

        mockMvc.perform(put("/sliders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("UpdatedSlider"));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        SliderRequest request = new SliderRequest("UpdatedSlider", "https://cdn.example.com/updated.png");

        when(sliderService.update(eq(99L), any(SliderRequest.class)))
                .thenThrow(new RuntimeException("Slider not found"));

        mockMvc.perform(put("/sliders/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Slider not found"));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/sliders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Slider deleted"));

        verify(sliderService).delete(1L);
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Slider not found")).when(sliderService).delete(99L);

        mockMvc.perform(delete("/sliders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Slider not found"));
    }
}
