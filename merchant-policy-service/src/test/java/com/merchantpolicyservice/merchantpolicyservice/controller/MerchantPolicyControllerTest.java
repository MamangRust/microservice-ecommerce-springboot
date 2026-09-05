package com.merchantpolicyservice.merchantpolicyservice.controller;

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

import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyMapper;
import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyMapperImpl;
import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyRequest;
import com.merchantpolicyservice.merchantpolicyservice.entity.MerchantPolicy;
import com.merchantpolicyservice.merchantpolicyservice.exc.GeneralExceptionHandler;
import com.merchantpolicyservice.merchantpolicyservice.service.MerchantPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class MerchantPolicyControllerTest {

    @Mock
    private MerchantPolicyService merchantPolicyService;

    private MockMvc mockMvc;

    private final MerchantPolicyMapper mapper = new MerchantPolicyMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        MerchantPolicyController controller = new MerchantPolicyController(merchantPolicyService, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private MerchantPolicy createPolicy(Long id, Long merchantId, String title) {
        MerchantPolicy policy = new MerchantPolicy();
        policy.setId(id);
        policy.setMerchantId(merchantId);
        policy.setPolicyType("RETURN");
        policy.setTitle(title);
        return policy;
    }

    @Test
    void getAll_returnsMappedList() throws Exception {
        when(merchantPolicyService.getAll()).thenReturn(List.of(createPolicy(1L, 1L, "Policy1")));

        mockMvc.perform(get("/merchant-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Policy1"))
                .andExpect(jsonPath("$[0].policyType").value("RETURN"));
    }

    @Test
    void getAll_returnsEmptyListWhenNone() throws Exception {
        when(merchantPolicyService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/merchant-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByMerchantId_returnsMappedList() throws Exception {
        when(merchantPolicyService.getByMerchantId(1L))
                .thenReturn(List.of(createPolicy(1L, 1L, "Policy1"), createPolicy(2L, 1L, "Policy1b")));

        mockMvc.perform(get("/merchant-policies/merchant/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Policy1"))
                .andExpect(jsonPath("$[1].title").value("Policy1b"));
    }

    @Test
    void getByMerchantId_returnsEmptyListWhenNoMatch() throws Exception {
        when(merchantPolicyService.getByMerchantId(42L)).thenReturn(List.of());

        mockMvc.perform(get("/merchant-policies/merchant/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_returnsResponse() throws Exception {
        when(merchantPolicyService.getById(1L)).thenReturn(createPolicy(1L, 1L, "Policy1"));

        mockMvc.perform(get("/merchant-policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Policy1"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(merchantPolicyService.getById(99L)).thenThrow(new RuntimeException("Policy not found"));

        mockMvc.perform(get("/merchant-policies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Policy not found"));
    }

    @Test
    void create_returnsCreatedResponse() throws Exception {
        MerchantPolicyRequest request = new MerchantPolicyRequest(1L, "REFUND", "NewPolicy", "new description");

        when(merchantPolicyService.create(any(MerchantPolicyRequest.class)))
                .thenReturn(createPolicy(5L, 1L, "NewPolicy"));

        mockMvc.perform(post("/merchant-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("NewPolicy"))
                .andExpect(jsonPath("$.merchantId").value(1));
    }

    @Test
    void create_returns409WhenDuplicateEntry() throws Exception {
        MerchantPolicyRequest request = new MerchantPolicyRequest(1L, "REFUND", "DupPolicy", null);

        when(merchantPolicyService.create(any(MerchantPolicyRequest.class)))
                .thenThrow(new DataIntegrityViolationException("dup"));

        mockMvc.perform(post("/merchant-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Duplicate entry"));
    }

    @Test
    void create_returns500WhenServiceFails() throws Exception {
        MerchantPolicyRequest request = new MerchantPolicyRequest(1L, "REFUND", "BrokenPolicy", null);

        when(merchantPolicyService.create(any(MerchantPolicyRequest.class)))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/merchant-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Error"))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void create_returns400WhenMerchantIdNull() throws Exception {
        MerchantPolicyRequest request = new MerchantPolicyRequest(null, "REFUND", "Title", null);

        mockMvc.perform(post("/merchant-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(merchantPolicyService, never()).create(any(MerchantPolicyRequest.class));
    }

    @Test
    void update_returnsUpdatedResponse() throws Exception {
        MerchantPolicyRequest request = new MerchantPolicyRequest(1L, "REFUND", "UpdatedPolicy", null);

        when(merchantPolicyService.update(org.mockito.ArgumentMatchers.eq(1L), any(MerchantPolicyRequest.class)))
                .thenReturn(createPolicy(1L, 1L, "UpdatedPolicy"));

        mockMvc.perform(put("/merchant-policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("UpdatedPolicy"));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        MerchantPolicyRequest request = new MerchantPolicyRequest(1L, "REFUND", "UpdatedPolicy", null);

        when(merchantPolicyService.update(org.mockito.ArgumentMatchers.eq(99L), any(MerchantPolicyRequest.class)))
                .thenThrow(new RuntimeException("Policy not found"));

        mockMvc.perform(put("/merchant-policies/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Policy not found"));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/merchant-policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Policy deleted"));

        verify(merchantPolicyService).delete(1L);
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Policy not found"))
                .when(merchantPolicyService).delete(99L);

        mockMvc.perform(delete("/merchant-policies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Policy not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        MerchantPolicy policy = createPolicy(1L, 1L, "Policy1");
        policy.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));
        policy.setUpdatedAt(LocalDateTime.of(2026, 9, 4, 11, 30));

        when(merchantPolicyService.getById(1L)).thenReturn(policy);

        mockMvc.perform(get("/merchant-policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026))
                .andExpect(jsonPath("$.updatedAt[0]").value(2026));
    }
}
