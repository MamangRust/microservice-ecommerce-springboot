package com.reviewdetailservice.reviewdetailservice.controller;

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
import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailMapper;
import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailMapperImpl;
import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailRequest;
import com.reviewdetailservice.reviewdetailservice.entity.ReviewDetail;
import com.reviewdetailservice.reviewdetailservice.exc.GeneralExceptionHandler;
import com.reviewdetailservice.reviewdetailservice.service.ReviewDetailService;

@ExtendWith(MockitoExtension.class)
class ReviewDetailControllerTest {

    @Mock
    private ReviewDetailService reviewDetailService;

    private MockMvc mockMvc;

    private final ReviewDetailMapper reviewDetailMapper = new ReviewDetailMapperImpl();

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        ReviewDetailController controller = new ReviewDetailController(reviewDetailService, reviewDetailMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GeneralExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private ReviewDetail createDetail(Long id, Long reviewId, String type, String url, String caption) {
        ReviewDetail detail = new ReviewDetail();
        detail.setId(id);
        detail.setReviewId(reviewId);
        detail.setType(type);
        detail.setUrl(url);
        detail.setCaption(caption);
        return detail;
    }

    @Test
    void getAllReviewDetails_returnsMappedList() throws Exception {
        when(reviewDetailService.getAll())
                .thenReturn(List.of(createDetail(1L, 10L, "image", "http://a.png", "caption1")));

        mockMvc.perform(get("/review-details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("image"))
                .andExpect(jsonPath("$[0].caption").value("caption1"));
    }

    @Test
    void getAllReviewDetails_returnsEmptyListWhenNone() throws Exception {
        when(reviewDetailService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/review-details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getDetailsByReview_returnsMappedList() throws Exception {
        when(reviewDetailService.getByReviewId(10L))
                .thenReturn(List.of(createDetail(1L, 10L, "video", "http://b.mp4", "caption2")));

        mockMvc.perform(get("/review-details/review/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(10))
                .andExpect(jsonPath("$[0].type").value("video"));
    }

    @Test
    void getReviewDetailById_returnsResponse() throws Exception {
        when(reviewDetailService.getById(1L))
                .thenReturn(createDetail(1L, 10L, "image", "http://a.png", "caption1"));

        mockMvc.perform(get("/review-details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reviewId").value(10))
                .andExpect(jsonPath("$.url").value("http://a.png"));
    }

    @Test
    void getReviewDetailById_returns404WhenNotFound() throws Exception {
        when(reviewDetailService.getById(99L)).thenThrow(new RuntimeException("Review detail not found"));

        mockMvc.perform(get("/review-details/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Review detail not found"));
    }

    @Test
    void createReviewDetail_returnsResponse() throws Exception {
        ReviewDetailRequest request = new ReviewDetailRequest(10L, "image", "http://a.png", "caption1");

        when(reviewDetailService.create(any(ReviewDetailRequest.class)))
                .thenReturn(createDetail(5L, 10L, "image", "http://a.png", "caption1"));

        mockMvc.perform(post("/review-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.type").value("image"));
    }

    @Test
    void createReviewDetail_returns400WhenReviewIdNull() throws Exception {
        String body = "{\"reviewId\": null, \"type\": \"image\", \"url\": \"http://a.png\", \"caption\": \"c\"}";

        mockMvc.perform(post("/review-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(reviewDetailService, never()).create(any(ReviewDetailRequest.class));
    }

    @Test
    void updateReviewDetail_returnsUpdatedResponse() throws Exception {
        ReviewDetailRequest request = new ReviewDetailRequest(10L, "video", "http://new.mp4", "new caption");

        when(reviewDetailService.update(eq(1L), any(ReviewDetailRequest.class)))
                .thenReturn(createDetail(1L, 10L, "video", "http://new.mp4", "new caption"));

        mockMvc.perform(put("/review-details/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("video"))
                .andExpect(jsonPath("$.caption").value("new caption"));
    }

    @Test
    void updateReviewDetail_returns404WhenNotFound() throws Exception {
        ReviewDetailRequest request = new ReviewDetailRequest(10L, "video", "http://new.mp4", "new caption");

        when(reviewDetailService.update(eq(99L), any(ReviewDetailRequest.class)))
                .thenThrow(new RuntimeException("Review detail not found"));

        mockMvc.perform(put("/review-details/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReviewDetail_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/review-details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Review detail deleted"));

        verify(reviewDetailService).delete(1L);
    }

    @Test
    void deleteReviewDetail_returns404WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Review detail not found")).when(reviewDetailService).delete(99L);

        mockMvc.perform(delete("/review-details/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Review detail not found"));
    }

    @Test
    void responseContainsTimestamps() throws Exception {
        ReviewDetail detail = createDetail(1L, 10L, "image", "http://a.png", "caption1");
        detail.setCreatedAt(LocalDateTime.of(2026, 9, 4, 10, 0));

        when(reviewDetailService.getById(1L)).thenReturn(detail);

        mockMvc.perform(get("/review-details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt[0]").value(2026));
    }
}
