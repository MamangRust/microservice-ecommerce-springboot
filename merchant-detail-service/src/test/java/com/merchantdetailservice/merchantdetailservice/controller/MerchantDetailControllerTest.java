package com.merchantdetailservice.merchantdetailservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailMapper;
import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailMapperImpl;
import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailRequest;
import com.merchantdetailservice.merchantdetailservice.entity.MerchantDetail;
import com.merchantdetailservice.merchantdetailservice.exc.GeneralExceptionHandler;
import com.merchantdetailservice.merchantdetailservice.service.MerchantDetailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class MerchantDetailControllerTest {

    @Mock
    private MerchantDetailService merchantDetailService;

    private MockMvc mockMvc;

    private final MerchantDetailMapper mapper = new MerchantDetailMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        MerchantDetailController controller = new MerchantDetailController(merchantDetailService, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private MerchantDetail createDetail(Long id, Long merchantId) {
        MerchantDetail detail = new MerchantDetail();
        detail.setId(id);
        detail.setMerchantId(merchantId);
        detail.setDisplayName("Merchant " + merchantId);
        return detail;
    }

    @Test
    void getAll_returnsMappedList() throws Exception {
        when(merchantDetailService.getAll()).thenReturn(List.of(createDetail(1L, 1L)));

        mockMvc.perform(get("/merchant-details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].merchantId").value(1))
                .andExpect(jsonPath("$[0].displayName").value("Merchant 1"));
    }

    @Test
    void getAll_returnsEmptyListWhenNone() throws Exception {
        when(merchantDetailService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/merchant-details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByMerchantId_returnsSingleObject() throws Exception {
        when(merchantDetailService.getByMerchantId(1L)).thenReturn(createDetail(1L, 1L));

        mockMvc.perform(get("/merchant-details/merchant/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.merchantId").value(1))
                .andExpect(jsonPath("$.displayName").value("Merchant 1"));
    }

    @Test
    void getByMerchantId_returns404WhenNotFound() throws Exception {
        when(merchantDetailService.getByMerchantId(42L))
                .thenThrow(new RuntimeException("Merchant detail not found"));

        mockMvc.perform(get("/merchant-details/merchant/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant detail not found"));
    }

    @Test
    void getById_returnsResponse() throws Exception {
        when(merchantDetailService.getById(1L)).thenReturn(createDetail(1L, 1L));

        mockMvc.perform(get("/merchant-details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.displayName").value("Merchant 1"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(merchantDetailService.getById(99L)).thenThrow(new RuntimeException("Merchant detail not found"));

        mockMvc.perform(get("/merchant-details/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant detail not found"));
    }

    @Test
    void create_returnsCreatedResponse() throws Exception {
        MerchantDetailRequest request = new MerchantDetailRequest(1L, "New Display", "https://new.example.com/cover",
                "https://new.example.com/logo", "new short desc", "https://new.example.com");

        when(merchantDetailService.create(any(MerchantDetailRequest.class)))
                .thenReturn(createDetail(5L, 1L));

        mockMvc.perform(post("/merchant-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.merchantId").value(1));
    }

    @Test
    void create_returns409WhenDuplicateEntry() throws Exception {
        MerchantDetailRequest request = new MerchantDetailRequest(1L, null, null, null, null, null);

        when(merchantDetailService.create(any(MerchantDetailRequest.class)))
                .thenThrow(new DataIntegrityViolationException("dup"));

        mockMvc.perform(post("/merchant-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Duplicate entry"));
    }

    @Test
    void create_returns500WhenServiceFails() throws Exception {
        MerchantDetailRequest request = new MerchantDetailRequest(1L, null, null, null, null, null);

        when(merchantDetailService.create(any(MerchantDetailRequest.class)))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/merchant-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Error"))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void create_returns400WhenMerchantIdNull() throws Exception {
        MerchantDetailRequest request = new MerchantDetailRequest(null, "New Display", null, null, null, null);

        mockMvc.perform(post("/merchant-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(merchantDetailService, never()).create(any(MerchantDetailRequest.class));
    }

    @Test
    void update_returnsUpdatedResponse() throws Exception {
        MerchantDetailRequest request = new MerchantDetailRequest(1L, "New Display", null, null, null, null);

        when(merchantDetailService.update(org.mockito.ArgumentMatchers.eq(1L), any(MerchantDetailRequest.class)))
                .thenReturn(createDetail(1L, 1L));

        mockMvc.perform(put("/merchant-details/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.displayName").value("Merchant 1"));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        MerchantDetailRequest request = new MerchantDetailRequest(1L, "New Display", null, null, null, null);

        when(merchantDetailService.update(org.mockito.ArgumentMatchers.eq(99L), any(MerchantDetailRequest.class)))
                .thenThrow(new RuntimeException("Merchant detail not found"));

        mockMvc.perform(put("/merchant-details/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant detail not found"));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/merchant-details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Merchant detail deleted"));

        verify(merchantDetailService).delete(1L);
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Merchant detail not found"))
                .when(merchantDetailService).delete(99L);

        mockMvc.perform(delete("/merchant-details/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant detail not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        MerchantDetail detail = createDetail(1L, 1L);
        detail.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));
        detail.setUpdatedAt(LocalDateTime.of(2026, 9, 4, 11, 30));

        when(merchantDetailService.getById(1L)).thenReturn(detail);

        mockMvc.perform(get("/merchant-details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026))
                .andExpect(jsonPath("$.updatedAt[0]").value(2026));
    }
}
