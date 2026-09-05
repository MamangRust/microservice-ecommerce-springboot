package com.bannerservice.bannerservice.controller;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import com.bannerservice.bannerservice.dto.BannerMapper;
import com.bannerservice.bannerservice.dto.BannerMapperImpl;
import com.bannerservice.bannerservice.dto.BannerRequest;
import com.bannerservice.bannerservice.entity.Banner;
import com.bannerservice.bannerservice.exc.GeneralExceptionHandler;
import com.bannerservice.bannerservice.service.BannerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class BannerControllerTest {

    @Mock
    private BannerService bannerService;

    private MockMvc mockMvc;

    private final BannerMapper bannerMapper = new BannerMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        BannerController controller = new BannerController(bannerService, bannerMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private Banner createBanner(Long id, String name, Boolean isActive) {
        Banner banner = new Banner();
        banner.setId(id);
        banner.setName(name);
        if (isActive != null) {
            banner.setIsActive(isActive);
        }
        return banner;
    }

    @Test
    void getAll_returnsMappedList() throws Exception {
        when(bannerService.getAll())
                .thenReturn(List.of(createBanner(1L, "Banner1", true)));

        mockMvc.perform(get("/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Banner1"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void getAll_returnsEmptyListWhenNone() throws Exception {
        when(bannerService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getActive_returnsMappedList() throws Exception {
        when(bannerService.getActive())
                .thenReturn(List.of(createBanner(2L, "ActiveBanner", true)));

        mockMvc.perform(get("/banners/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void getById_returnsResponse() throws Exception {
        when(bannerService.getById(1L)).thenReturn(createBanner(1L, "Banner1", true));

        mockMvc.perform(get("/banners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Banner1"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(bannerService.getById(99L)).thenThrow(new RuntimeException("Banner not found"));

        mockMvc.perform(get("/banners/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Banner not found"));
    }

    @Test
    void create_returnsCreatedResponse() throws Exception {
        BannerRequest request = new BannerRequest("NewBanner",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalTime.of(8, 0), LocalTime.of(22, 0), true);

        when(bannerService.create(any(BannerRequest.class)))
                .thenReturn(createBanner(5L, "NewBanner", true));

        mockMvc.perform(post("/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("NewBanner"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void create_returns500WhenServiceFails() throws Exception {
        BannerRequest request = new BannerRequest("NewBanner",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalTime.of(8, 0), LocalTime.of(22, 0), true);

        when(bannerService.create(any(BannerRequest.class)))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void create_returns400WhenNameBlank() throws Exception {
        BannerRequest request = new BannerRequest(" ",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalTime.of(8, 0), LocalTime.of(22, 0), true);

        mockMvc.perform(post("/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(bannerService, never()).create(any(BannerRequest.class));
    }

    @Test
    void update_returnsUpdatedResponse() throws Exception {
        BannerRequest request = new BannerRequest("UpdatedName",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                LocalTime.of(9, 0), LocalTime.of(21, 0), true);

        when(bannerService.update(eq(1L), any(BannerRequest.class)))
                .thenReturn(createBanner(1L, "UpdatedName", true));

        mockMvc.perform(put("/banners/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("UpdatedName"));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        BannerRequest request = new BannerRequest("UpdatedName",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                LocalTime.of(9, 0), LocalTime.of(21, 0), true);

        when(bannerService.update(eq(99L), any(BannerRequest.class)))
                .thenThrow(new RuntimeException("Banner not found"));

        mockMvc.perform(put("/banners/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Banner not found"));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/banners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Banner deleted"));

        verify(bannerService).delete(1L);
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Banner not found")).when(bannerService).delete(99L);

        mockMvc.perform(delete("/banners/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Banner not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        Banner banner = createBanner(1L, "Banner1", true);
        banner.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));

        when(bannerService.getById(1L)).thenReturn(banner);

        mockMvc.perform(get("/banners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026));
    }
}
