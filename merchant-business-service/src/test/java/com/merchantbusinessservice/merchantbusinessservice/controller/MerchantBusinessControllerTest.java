package com.merchantbusinessservice.merchantbusinessservice.controller;

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

import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessMapper;
import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessMapperImpl;
import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessRequest;
import com.merchantbusinessservice.merchantbusinessservice.entity.MerchantBusinessInformation;
import com.merchantbusinessservice.merchantbusinessservice.exc.GeneralExceptionHandler;
import com.merchantbusinessservice.merchantbusinessservice.service.MerchantBusinessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class MerchantBusinessControllerTest {

    @Mock
    private MerchantBusinessService merchantBusinessService;

    private MockMvc mockMvc;

    private final MerchantBusinessMapper mapper = new MerchantBusinessMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        MerchantBusinessController controller = new MerchantBusinessController(merchantBusinessService, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private MerchantBusinessInformation createBusiness(Long id, Long merchantId) {
        MerchantBusinessInformation business = new MerchantBusinessInformation();
        business.setId(id);
        business.setMerchantId(merchantId);
        business.setBusinessType("PT");
        business.setEstablishedYear(2020);
        return business;
    }

    @Test
    void getAll_returnsMappedList() throws Exception {
        when(merchantBusinessService.getAll()).thenReturn(List.of(createBusiness(1L, 1L)));

        mockMvc.perform(get("/merchant-businesses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].merchantId").value(1))
                .andExpect(jsonPath("$[0].businessType").value("PT"));
    }

    @Test
    void getAll_returnsEmptyListWhenNone() throws Exception {
        when(merchantBusinessService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/merchant-businesses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByMerchantId_returnsSingleObject() throws Exception {
        when(merchantBusinessService.getByMerchantId(1L)).thenReturn(createBusiness(1L, 1L));

        mockMvc.perform(get("/merchant-businesses/merchant/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.merchantId").value(1))
                .andExpect(jsonPath("$.businessType").value("PT"));
    }

    @Test
    void getByMerchantId_returns404WhenNotFound() throws Exception {
        when(merchantBusinessService.getByMerchantId(42L))
                .thenThrow(new RuntimeException("Merchant business not found"));

        mockMvc.perform(get("/merchant-businesses/merchant/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant business not found"));
    }

    @Test
    void getById_returnsResponse() throws Exception {
        when(merchantBusinessService.getById(1L)).thenReturn(createBusiness(1L, 1L));

        mockMvc.perform(get("/merchant-businesses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.businessType").value("PT"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(merchantBusinessService.getById(99L)).thenThrow(new RuntimeException("Merchant business not found"));

        mockMvc.perform(get("/merchant-businesses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant business not found"));
    }

    @Test
    void create_returnsCreatedResponse() throws Exception {
        MerchantBusinessRequest request = new MerchantBusinessRequest(1L, "CV", "TAX", 2021, 25, "https://new.example.com");

        when(merchantBusinessService.create(any(MerchantBusinessRequest.class)))
                .thenReturn(createBusiness(5L, 1L));

        mockMvc.perform(post("/merchant-businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.merchantId").value(1));
    }

    @Test
    void create_returns409WhenDuplicateEntry() throws Exception {
        MerchantBusinessRequest request = new MerchantBusinessRequest(1L, null, null, null, null, null);

        when(merchantBusinessService.create(any(MerchantBusinessRequest.class)))
                .thenThrow(new DataIntegrityViolationException("dup"));

        mockMvc.perform(post("/merchant-businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Duplicate entry"));
    }

    @Test
    void create_returns500WhenServiceFails() throws Exception {
        MerchantBusinessRequest request = new MerchantBusinessRequest(1L, null, null, null, null, null);

        when(merchantBusinessService.create(any(MerchantBusinessRequest.class)))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/merchant-businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Error"))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void create_returns400WhenMerchantIdNull() throws Exception {
        MerchantBusinessRequest request = new MerchantBusinessRequest(null, "CV", null, null, null, null);

        mockMvc.perform(post("/merchant-businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(merchantBusinessService, never()).create(any(MerchantBusinessRequest.class));
    }

    @Test
    void update_returnsUpdatedResponse() throws Exception {
        MerchantBusinessRequest request = new MerchantBusinessRequest(1L, "CV", "TAX", 2021, 25, "https://new.example.com");

        when(merchantBusinessService.update(org.mockito.ArgumentMatchers.eq(1L), any(MerchantBusinessRequest.class)))
                .thenReturn(createBusiness(1L, 1L));

        mockMvc.perform(put("/merchant-businesses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.businessType").value("PT"));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        MerchantBusinessRequest request = new MerchantBusinessRequest(1L, "CV", null, null, null, null);

        when(merchantBusinessService.update(org.mockito.ArgumentMatchers.eq(99L), any(MerchantBusinessRequest.class)))
                .thenThrow(new RuntimeException("Merchant business not found"));

        mockMvc.perform(put("/merchant-businesses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant business not found"));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/merchant-businesses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Merchant business deleted"));

        verify(merchantBusinessService).delete(1L);
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Merchant business not found"))
                .when(merchantBusinessService).delete(99L);

        mockMvc.perform(delete("/merchant-businesses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant business not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        MerchantBusinessInformation business = createBusiness(1L, 1L);
        business.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));
        business.setUpdatedAt(LocalDateTime.of(2026, 9, 4, 11, 30));

        when(merchantBusinessService.getById(1L)).thenReturn(business);

        mockMvc.perform(get("/merchant-businesses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026))
                .andExpect(jsonPath("$.updatedAt[0]").value(2026));
    }
}
