package com.shippingaddressservice.shippingaddressservice.controller;

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

import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressMapper;
import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressMapperImpl;
import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressRequest;
import com.shippingaddressservice.shippingaddressservice.entity.ShippingAddress;
import com.shippingaddressservice.shippingaddressservice.exc.GeneralExceptionHandler;
import com.shippingaddressservice.shippingaddressservice.service.ShippingAddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class ShippingAddressControllerTest {

    @Mock
    private ShippingAddressService shippingAddressService;

    private MockMvc mockMvc;

    private final ShippingAddressMapper shippingAddressMapper = new ShippingAddressMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        ShippingAddressController controller = new ShippingAddressController(shippingAddressService, shippingAddressMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private ShippingAddress createAddress(Long id, Long orderId) {
        ShippingAddress address = new ShippingAddress();
        address.setId(id);
        address.setOrderId(orderId);
        address.setAlamat("Jl. Merdeka No. 10");
        address.setProvinsi("Jawa Barat");
        address.setNegara("Indonesia");
        address.setKota("Bandung");
        address.setCourier("jne");
        address.setShippingMethod("REG");
        address.setShippingCost(20_000);
        return address;
    }

    @Test
    void getAll_returnsMappedList() throws Exception {
        when(shippingAddressService.getAll()).thenReturn(List.of(createAddress(1L, 100L)));

        mockMvc.perform(get("/shipping-addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(100))
                .andExpect(jsonPath("$[0].kota").value("Bandung"));
    }

    @Test
    void getAll_returnsEmptyListWhenNone() throws Exception {
        when(shippingAddressService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/shipping-addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByOrder_returnsMappedList() throws Exception {
        when(shippingAddressService.getByOrderId(100L)).thenReturn(List.of(createAddress(1L, 100L)));

        mockMvc.perform(get("/shipping-addresses/order/{orderId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(100));
    }

    @Test
    void getByOrder_returnsEmptyListWhenNoMatch() throws Exception {
        when(shippingAddressService.getByOrderId(42L)).thenReturn(List.of());

        mockMvc.perform(get("/shipping-addresses/order/{orderId}", 42L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_returnsResponse() throws Exception {
        when(shippingAddressService.getById(1L)).thenReturn(createAddress(1L, 100L));

        mockMvc.perform(get("/shipping-addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alamat").value("Jl. Merdeka No. 10"))
                .andExpect(jsonPath("$.shippingCost").value(20_000));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(shippingAddressService.getById(99L)).thenThrow(new RuntimeException("Address not found"));

        mockMvc.perform(get("/shipping-addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Address not found"));
    }

    @Test
    void create_returnsCreatedResponse() throws Exception {
        ShippingAddressRequest request = new ShippingAddressRequest(100L, "Jl. Sukajadi No. 5",
                "Jawa Barat", "Indonesia", "Bandung", "jne", "REG", 25_000);

        when(shippingAddressService.create(any(ShippingAddressRequest.class)))
                .thenReturn(createAddress(5L, 100L));

        mockMvc.perform(post("/shipping-addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.kota").value("Bandung"));
    }

    @Test
    void create_returns500WhenServiceFails() throws Exception {
        ShippingAddressRequest request = new ShippingAddressRequest(100L, "Jl. Sukajadi No. 5",
                "Jawa Barat", "Indonesia", "Bandung", "jne", "REG", 25_000);

        when(shippingAddressService.create(any(ShippingAddressRequest.class)))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/shipping-addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    @Test
    void create_returns400WhenOrderIdNull() throws Exception {
        String body = "{\"orderId\": null, \"alamat\": \"Jl. Sukajadi No. 5\", \"kota\": \"Bandung\"}";

        mockMvc.perform(post("/shipping-addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(shippingAddressService, never()).create(any(ShippingAddressRequest.class));
    }

    @Test
    void update_returnsUpdatedResponse() throws Exception {
        ShippingAddressRequest request = new ShippingAddressRequest(100L, "Jl. Baru No. 1",
                "DKI Jakarta", "Indonesia", "Jakarta", "jnt", "ECO", 15_000);

        when(shippingAddressService.update(eq(1L), any(ShippingAddressRequest.class)))
                .thenReturn(createAddress(1L, 100L));

        mockMvc.perform(put("/shipping-addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(100));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        ShippingAddressRequest request = new ShippingAddressRequest(100L, "Jl. Baru No. 1",
                "DKI Jakarta", "Indonesia", "Jakarta", "jnt", "ECO", 15_000);

        when(shippingAddressService.update(eq(99L), any(ShippingAddressRequest.class)))
                .thenThrow(new RuntimeException("Address not found"));

        mockMvc.perform(put("/shipping-addresses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Address not found"));
    }

    @Test
    void delete_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/shipping-addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Deleted"));

        verify(shippingAddressService).delete(1L);
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Address not found")).when(shippingAddressService).delete(99L);

        mockMvc.perform(delete("/shipping-addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Address not found"));
    }
}
