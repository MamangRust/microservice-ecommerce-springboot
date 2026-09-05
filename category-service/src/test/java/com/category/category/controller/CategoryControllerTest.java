package com.category.category.controller;

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

import java.time.LocalDateTime;
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

import com.category.category.dto.CategoryMapper;
import com.category.category.dto.CategoryMapperImpl;
import com.category.category.dto.CategoryRequest;
import com.category.category.entity.Category;
import com.category.category.exc.GeneralExceptionHandler;
import com.category.category.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    private final CategoryMapper categoryMapper = new CategoryMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryService, categoryMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private Category createCategory(Long id, String name, String description) {
        Category category = new Category();
        category.setCategoryId(id);
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    @Test
    void getAllCategories_returnsMappedList() throws Exception {
        when(categoryService.getAll())
                .thenReturn(List.of(createCategory(1L, "Electronics", "Devices and gadgets")));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    void getAllCategories_returnsEmptyListWhenNone() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCategoryById_returnsResponse() throws Exception {
        when(categoryService.getById(1L))
                .thenReturn(createCategory(1L, "Electronics", "Devices and gadgets"));

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryById_returns404WhenNotFound() throws Exception {
        when(categoryService.getById(99L)).thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(get("/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Category not found"));
    }

    @Test
    void createCategory_returnsResponse() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics", "Devices and gadgets");

        when(categoryService.create(any(CategoryRequest.class)))
                .thenReturn(createCategory(5L, "Electronics", "Devices and gadgets"));

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(5))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategory_returns409WhenAlreadyExists() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics", "Duplicate");

        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("Category already exists"));

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value("Category already exists"));
    }

    @Test
    void createCategory_returns500OnOtherServiceError() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics", "Devices and gadgets");

        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("db down"));
    }

    @Test
    void createCategory_returns400WhenNameBlank() throws Exception {
        CategoryRequest request = new CategoryRequest("  ", null);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).create(any(CategoryRequest.class));
    }

    @Test
    void createCategory_returns400WhenNameTooLong() throws Exception {
        String longName = "X".repeat(101);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + longName + "\", \"description\": \"d\"}"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).create(any(CategoryRequest.class));
    }

    @Test
    void updateCategory_returnsUpdatedResponse() throws Exception {
        CategoryRequest request = new CategoryRequest("UpdatedName", "NewDescription");

        when(categoryService.update(eq(1L), any(CategoryRequest.class)))
                .thenReturn(createCategory(1L, "UpdatedName", "NewDescription"));

        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.description").value("NewDescription"));
    }

    @Test
    void updateCategory_returns404WhenNotFound() throws Exception {
        CategoryRequest request = new CategoryRequest("UpdatedName", "NewDescription");

        when(categoryService.update(eq(99L), any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(put("/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Category deleted"));

        verify(categoryService).delete(1L);
    }

    @Test
    void deleteCategory_returns404WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Category not found")).when(categoryService).delete(99L);

        mockMvc.perform(delete("/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Category not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        Category category = createCategory(1L, "Electronics", "Devices and gadgets");
        category.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));

        when(categoryService.getById(1L)).thenReturn(category);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026));
    }
}
