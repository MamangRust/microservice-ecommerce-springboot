package com.reviewservice.reviewservice.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reviewservice.reviewservice.dto.ReviewMapper;
import com.reviewservice.reviewservice.dto.ReviewMapperImpl;
import com.reviewservice.reviewservice.dto.ReviewRequest;
import com.reviewservice.reviewservice.entity.Review;
import com.reviewservice.reviewservice.exc.GeneralExceptionHandler;
import com.reviewservice.reviewservice.service.ReviewService;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    private MockMvc mockMvc;

    private final ReviewMapper reviewMapper = new ReviewMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        ReviewController controller = new ReviewController(reviewService, reviewMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private Review createReview(Long id, Long userId, Long productId, String name, Integer rating) {
        Review review = new Review();
        review.setId(id);
        review.setUserId(userId);
        review.setProductId(productId);
        review.setName(name);
        review.setComment("comment");
        review.setRating(rating);
        return review;
    }

    @Test
    void getAllReviews_returnsMappedList() throws Exception {
        when(reviewService.getAll()).thenReturn(List.of(createReview(1L, 1L, 5L, "Review1", 5)));

        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Review1"))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void getAllReviews_returnsEmptyListWhenNone() throws Exception {
        when(reviewService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getReviewsByProduct_returnsMappedList() throws Exception {
        when(reviewService.getByProductId(5L))
                .thenReturn(List.of(createReview(1L, 1L, 5L, "ProductReview", 4)));

        mockMvc.perform(get("/reviews/product/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(5))
                .andExpect(jsonPath("$[0].name").value("ProductReview"));
    }

    @Test
    void getReviewById_returnsResponse() throws Exception {
        when(reviewService.getById(1L)).thenReturn(createReview(1L, 1L, 5L, "Review1", 5));

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.productId").value(5));
    }

    @Test
    void getReviewById_returns404WhenNotFound() throws Exception {
        when(reviewService.getById(99L)).thenThrow(new RuntimeException("Review not found"));

        mockMvc.perform(get("/reviews/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Review not found"));
    }

    @Test
    void createReview_returnsResponse() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 5L, "NewReview", "comment", 5);

        when(reviewService.create(any(ReviewRequest.class)))
                .thenReturn(createReview(5L, 1L, 5L, "NewReview", 5));

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("NewReview"));
    }

    @Test
    void createReview_returns400WhenRatingBelowMin() throws Exception {
        String body = "{\"userId\": 1, \"productId\": 5, \"name\": \"X\", \"comment\": \"c\", \"rating\": 0}";

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).create(any(ReviewRequest.class));
    }

    @Test
    void createReview_returns400WhenRatingAboveMax() throws Exception {
        String body = "{\"userId\": 1, \"productId\": 5, \"name\": \"X\", \"comment\": \"c\", \"rating\": 6}";

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).create(any(ReviewRequest.class));
    }

    @Test
    void createReview_returns400WhenUserIdNull() throws Exception {
        String body = "{\"userId\": null, \"productId\": 5, \"name\": \"X\", \"comment\": \"c\", \"rating\": 4}";

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).create(any(ReviewRequest.class));
    }

    @Test
    void updateReview_returnsUpdatedResponse() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 5L, "UpdatedName", "updated", 3);

        when(reviewService.update(eq(1L), any(ReviewRequest.class)))
                .thenReturn(createReview(1L, 1L, 5L, "UpdatedName", 3));

        mockMvc.perform(put("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.rating").value(3));
    }

    @Test
    void updateReview_returns404WhenNotFound() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 5L, "UpdatedName", "updated", 3);

        when(reviewService.update(eq(99L), any(ReviewRequest.class)))
                .thenThrow(new RuntimeException("Review not found"));

        mockMvc.perform(put("/reviews/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Review deleted"));

        verify(reviewService).delete(1L);
    }

    @Test
    void deleteReview_returns404WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Review not found")).when(reviewService).delete(99L);

        mockMvc.perform(delete("/reviews/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Review not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        Review review = createReview(1L, 1L, 5L, "Review1", 5);
        review.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));

        when(reviewService.getById(1L)).thenReturn(review);

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026));
    }
}
