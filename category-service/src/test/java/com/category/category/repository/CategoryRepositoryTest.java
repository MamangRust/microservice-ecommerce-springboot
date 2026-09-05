package com.category.category.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.category.category.entity.Category;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class CategoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private CategoryRepository categoryRepository;

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    @Test
    void save_persistsCategoryWithGeneratedIdAndTimestamps() {
        Category saved = categoryRepository.save(createCategory("Electronics", "Devices and gadgets"));

        assertThat(saved.getCategoryId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void save_autoDerivesSlugFromNameWhenSlugNull() {
        Category saved = categoryRepository.save(createCategory("Gaming Laptops", null));

        assertThat(saved.getSlugCategory()).isEqualTo("gaming-laptops");
    }

    @Test
    void save_autoDerivesSlugTrimsAndCollapsesWhitespace() {
        Category saved = categoryRepository.save(createCategory("  Home   Appliances  ", null));

        assertThat(saved.getSlugCategory()).isEqualTo("home-appliances");
    }

    @Test
    void save_keepsProvidedSlugWhenSet() {
        Category category = createCategory("Electronics", null);
        category.setSlugCategory("custom-slug");

        Category saved = categoryRepository.save(category);

        assertThat(saved.getSlugCategory()).isEqualTo("custom-slug");
    }

    @Test
    void findById_returnsSavedCategory() {
        Category saved = categoryRepository.save(createCategory("Electronics", "Devices and gadgets"));

        Optional<Category> found = categoryRepository.findById(saved.getCategoryId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
        assertThat(found.get().getDescription()).isEqualTo("Devices and gadgets");
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        Optional<Category> found = categoryRepository.findById(999999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllPersisted() {
        categoryRepository.save(createCategory("Electronics", "Devices"));
        categoryRepository.save(createCategory("Fashion", "Clothing"));

        List<Category> all = categoryRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Category::getName).containsExactlyInAnyOrder("Electronics", "Fashion");
    }

    @Test
    void findBySlugCategory_returnsCategoryWhenFound() {
        categoryRepository.save(createCategory("Gaming Laptops", null));

        Optional<Category> found = categoryRepository.findBySlugCategory("gaming-laptops");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Gaming Laptops");
    }

    @Test
    void findBySlugCategory_returnsEmptyWhenMissing() {
        categoryRepository.save(createCategory("Gaming Laptops", null));

        Optional<Category> found = categoryRepository.findBySlugCategory("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void save_rejectsDuplicateSlugOnUniqueConstraint() {
        categoryRepository.saveAndFlush(createCategory("Electronics", "First"));

        Category duplicate = createCategory("Electronics", "Second");
        duplicate.setSlugCategory("electronics");

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void update_touchesUpdatedAtViaPreUpdate() {
        Category saved = categoryRepository.save(createCategory("Electronics", "Before"));
        LocalDateTime createdAtBefore = saved.getCreatedAt();

        saved.setDescription("After");
        Category updated = categoryRepository.saveAndFlush(saved);

        assertThat(updated.getDescription()).isEqualTo("After");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAtBefore);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteById_removesRow() {
        Category saved = categoryRepository.save(createCategory("DeleteMe", null));

        categoryRepository.deleteById(saved.getCategoryId());
        categoryRepository.flush();

        assertThat(categoryRepository.findById(saved.getCategoryId())).isEmpty();
    }
}
