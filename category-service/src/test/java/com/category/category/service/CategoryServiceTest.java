package com.category.category.service;

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

import com.category.category.dto.CategoryMapper;
import com.category.category.dto.CategoryMapperImpl;
import com.category.category.dto.CategoryRequest;
import com.category.category.entity.Category;
import com.category.category.repository.CategoryRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    private final CategoryMapper categoryMapper = new CategoryMapperImpl();

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, categoryMapper, OpenTelemetry.noop());
    }

    private Category createCategory(Long id, String name, String description) {
        Category category = new Category();
        category.setCategoryId(id);
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    private CategoryRequest createRequest(String name, String description) {
        return new CategoryRequest(name, description);
    }

    @Test
    void getAll_returnsAllFromRepository() {
        Category c1 = createCategory(1L, "Electronics", "Devices and gadgets");
        Category c2 = createCategory(2L, "Fashion", "Clothing and accessories");

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Category> result = categoryService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName).containsExactly("Electronics", "Fashion");
        verify(categoryRepository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNoCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<Category> result = categoryService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getById_returnsCategoryWhenFound() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(createCategory(1L, "Electronics", "Devices and gadgets")));

        Category result = categoryService.getById(1L);

        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        Category saved = createCategory(5L, "Electronics", "Devices and gadgets");

        when(categoryRepository.findBySlugCategory("electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        Category result = categoryService.create(createRequest("Electronics", "Devices and gadgets"));

        assertThat(result.getCategoryId()).isEqualTo(5L);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Electronics");
        assertThat(captor.getValue().getDescription()).isEqualTo("Devices and gadgets");
        // slugCategory is left to @PrePersist — the mapper ignores it and the
        // service only derives a slug for the duplicate check
        assertThat(captor.getValue().getSlugCategory()).isNull();
    }

    @Test
    void create_normalizesNameToSlugForDuplicateCheck() {
        when(categoryRepository.findBySlugCategory("gaming-laptops")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(createCategory(5L, "Gaming Laptops", null));

        categoryService.create(createRequest("Gaming Laptops", null));

        verify(categoryRepository).findBySlugCategory("gaming-laptops");
    }

    @Test
    void create_throwsWhenSlugAlreadyExists() {
        when(categoryRepository.findBySlugCategory("electronics"))
                .thenReturn(Optional.of(createCategory(1L, "Electronics", "Existing")));

        assertThatThrownBy(() -> categoryService.create(createRequest("Electronics", "Duplicate")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category already exists");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void update_updatesNameAndDescriptionButNotSlug() {
        Category existing = createCategory(1L, "OldName", "OldDescription");
        existing.setSlugCategory("oldname");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.update(1L, createRequest("NewName", "NewDescription"));

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getDescription()).isEqualTo("NewDescription");
        assertThat(result.getSlugCategory()).isEqualTo("oldname");
        verify(categoryRepository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(999L, createRequest("X", null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void delete_delegatesToRepository() {
        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
