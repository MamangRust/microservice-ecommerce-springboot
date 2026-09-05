package com.merchantawardservice.merchantawardservice.controller;

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

import java.time.LocalDate;
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

import com.merchantawardservice.merchantawardservice.dto.MerchantAwardMapper;
import com.merchantawardservice.merchantawardservice.dto.MerchantAwardMapperImpl;
import com.merchantawardservice.merchantawardservice.dto.MerchantAwardRequest;
import com.merchantawardservice.merchantawardservice.entity.MerchantCertificationAndAward;
import com.merchantawardservice.merchantawardservice.exc.GeneralExceptionHandler;
import com.merchantawardservice.merchantawardservice.service.MerchantAwardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class MerchantAwardControllerTest {

    @Mock
    private MerchantAwardService merchantAwardService;

    private MockMvc mockMvc;

    private final MerchantAwardMapper mapper = new MerchantAwardMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        MerchantAwardController controller = new MerchantAwardController(merchantAwardService, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private MerchantCertificationAndAward createAward(Long id, Long merchantId, String title) {
        MerchantCertificationAndAward award = new MerchantCertificationAndAward();
        award.setId(id);
        award.setMerchantId(merchantId);
        award.setTitle(title);
        award.setIssueDate(LocalDate.of(2024, 1, 15));
        award.setExpiryDate(LocalDate.of(2026, 1, 15));
        return award;
    }

    @Test
    void getAll_returnsMappedList() throws Exception {
        when(merchantAwardService.getAll()).thenReturn(List.of(createAward(1L, 1L, "Award1")));

        mockMvc.perform(get("/merchant-awards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Award1"))
                .andExpect(jsonPath("$[0].merchantId").value(1));
    }

    @Test
    void getAll_returnsEmptyListWhenNone() throws Exception {
        when(merchantAwardService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/merchant-awards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByMerchantId_returnsMappedList() throws Exception {
        when(merchantAwardService.getByMerchantId(1L)).thenReturn(List.of(createAward(1L, 1L, "Award1")));

        mockMvc.perform(get("/merchant-awards/merchant/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Award1"))
                .andExpect(jsonPath("$[0].merchantId").value(1));
    }

    @Test
    void getByMerchantId_returnsEmptyListWhenNoMatch() throws Exception {
        when(merchantAwardService.getByMerchantId(42L)).thenReturn(List.of());

        mockMvc.perform(get("/merchant-awards/merchant/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_returnsResponse() throws Exception {
        when(merchantAwardService.getById(1L)).thenReturn(createAward(1L, 1L, "Award1"));

        mockMvc.perform(get("/merchant-awards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Award1"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(merchantAwardService.getById(99L)).thenThrow(new RuntimeException("Merchant award not found"));

        mockMvc.perform(get("/merchant-awards/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant award not found"));
    }

    @Test
    void create_returnsCreatedResponse() throws Exception {
        MerchantAwardRequest request = new MerchantAwardRequest(1L, "NewAward", "desc", "Issuer",
                LocalDate.of(2025, 2, 1), LocalDate.of(2027, 2, 1), "https://cert.example.com/new");

        when(merchantAwardService.create(any(MerchantAwardRequest.class)))
                .thenReturn(createAward(5L, 1L, "NewAward"));

        mockMvc.perform(post("/merchant-awards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("NewAward"))
                .andExpect(jsonPath("$.issueDate[0]").value(2024));
    }

    @Test
    void create_returns409WhenDuplicateEntry() throws Exception {
        MerchantAwardRequest request = new MerchantAwardRequest(1L, "DupAward", null, null, null, null, null);

        when(merchantAwardService.create(any(MerchantAwardRequest.class)))
                .thenThrow(new DataIntegrityViolationException("dup"));

        mockMvc.perform(post("/merchant-awards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Duplicate entry"));
    }

    @Test
    void create_returns500WhenServiceFails() throws Exception {
        MerchantAwardRequest request = new MerchantAwardRequest(1L, "BrokenAward", null, null, null, null, null);

        when(merchantAwardService.create(any(MerchantAwardRequest.class)))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/merchant-awards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Error"))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void create_returns400WhenTitleBlank() throws Exception {
        MerchantAwardRequest request = new MerchantAwardRequest(1L, " ", null, null, null, null, null);

        mockMvc.perform(post("/merchant-awards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(merchantAwardService, never()).create(any(MerchantAwardRequest.class));
    }

    @Test
    void create_returns400WhenMerchantIdNull() throws Exception {
        MerchantAwardRequest request = new MerchantAwardRequest(null, "Title", null, null, null, null, null);

        mockMvc.perform(post("/merchant-awards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(merchantAwardService, never()).create(any(MerchantAwardRequest.class));
    }

    @Test
    void update_returnsUpdatedResponse() throws Exception {
        MerchantAwardRequest request = new MerchantAwardRequest(1L, "UpdatedAward", null, null, null, null, null);

        when(merchantAwardService.update(org.mockito.ArgumentMatchers.eq(1L), any(MerchantAwardRequest.class)))
                .thenReturn(createAward(1L, 1L, "UpdatedAward"));

        mockMvc.perform(put("/merchant-awards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("UpdatedAward"));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        MerchantAwardRequest request = new MerchantAwardRequest(1L, "UpdatedAward", null, null, null, null, null);

        when(merchantAwardService.update(org.mockito.ArgumentMatchers.eq(99L), any(MerchantAwardRequest.class)))
                .thenThrow(new RuntimeException("Merchant award not found"));

        mockMvc.perform(put("/merchant-awards/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant award not found"));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/merchant-awards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Merchant award deleted"));

        verify(merchantAwardService).delete(1L);
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Merchant award not found"))
                .when(merchantAwardService).delete(99L);

        mockMvc.perform(delete("/merchant-awards/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Merchant award not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        MerchantCertificationAndAward award = createAward(1L, 1L, "Award1");
        award.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));
        award.setUpdatedAt(LocalDateTime.of(2026, 9, 4, 11, 30));

        when(merchantAwardService.getById(1L)).thenReturn(award);

        mockMvc.perform(get("/merchant-awards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026))
                .andExpect(jsonPath("$.updatedAt[0]").value(2026))
                .andExpect(jsonPath("$.expiryDate[0]").value(2026));
    }
}
