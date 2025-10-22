package com.platoons.e_commerce;

import com.platoons.e_commerce.controller.ReviewController;
import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.dto.ReviewDto;
import com.platoons.e_commerce.service.IReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReviewControllerTests {

    private IReviewService reviewService;
    private ReviewController reviewController;
    private Authentication authentication;
    private String productId;
    private Long reviewId;

    @BeforeEach
    void setUp() {
        reviewService = mock(IReviewService.class);
        reviewController = new ReviewController(reviewService);
        authentication = mock(Authentication.class);
        productId = UUID.randomUUID().toString();
        reviewId = 1L;
    }

    @Test
    void createReview_returnsCreatedResponse() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        dto.setComment("Great product! Highly recommended.");
        dto.setRating(5);

        String username = "john_doe";
        when(authentication.getName()).thenReturn(username);
        when(reviewService.createReview(eq(productId), eq(dto), eq(username))).thenReturn(reviewId);

        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyLong())).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/review/" + reviewId));
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = reviewController.createReview(productId, dto, authentication);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Successfully created review", response.getBody().getMessage());
            verify(reviewService, times(1)).createReview(eq(productId), eq(dto), eq(username));
            verify(authentication, times(1)).getName();
        }
    }

    @Test
    void createReview_withMinimumRating_returnsCreatedResponse() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        dto.setComment("Not satisfied with the product.");
        dto.setRating(1);

        String username = "jane_doe";
        when(authentication.getName()).thenReturn(username);
        when(reviewService.createReview(eq(productId), eq(dto), eq(username))).thenReturn(reviewId);

        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyLong())).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/review/" + reviewId));
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = reviewController.createReview(productId, dto, authentication);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Successfully created review", response.getBody().getMessage());
            verify(reviewService, times(1)).createReview(eq(productId), eq(dto), eq(username));
        }
    }

    @Test
    void createReview_withMaximumRating_returnsCreatedResponse() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        dto.setComment("Absolutely perfect! Best purchase ever.");
        dto.setRating(5);

        String username = "test_user";
        when(authentication.getName()).thenReturn(username);
        when(reviewService.createReview(eq(productId), eq(dto), eq(username))).thenReturn(reviewId);

        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyLong())).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/review/" + reviewId));
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = reviewController.createReview(productId, dto, authentication);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(reviewService, times(1)).createReview(eq(productId), eq(dto), eq(username));
        }
    }

    @Test
    void getReviewsByProductId_returnsListOfReviews() {
        ReviewDto review1 = new ReviewDto();
        review1.setReviewId(1L);
        review1.setUsername("user1");
        review1.setContent("Great product!");
        review1.setRating(5);
        review1.setProfilePictureUrl("https://example.com/user1.jpg");

        ReviewDto review2 = new ReviewDto();
        review2.setReviewId(2L);
        review2.setUsername("user2");
        review2.setContent("Good quality.");
        review2.setRating(4);
        review2.setProfilePictureUrl("https://example.com/user2.jpg");

        List<ReviewDto> reviews = Arrays.asList(review1, review2);

        when(reviewService.getReviewsByProductId(productId)).thenReturn(reviews);

        ResponseEntity<List<ReviewDto>> response = reviewController.getReviewsByProductId(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("user1", response.getBody().getFirst().getUsername());
        assertEquals("Great product!", response.getBody().get(0).getContent());
        assertEquals(5, response.getBody().get(0).getRating());
        assertEquals("user2", response.getBody().get(1).getUsername());
        assertEquals("Good quality.", response.getBody().get(1).getContent());
        assertEquals(4, response.getBody().get(1).getRating());
        verify(reviewService, times(1)).getReviewsByProductId(productId);
    }

    @Test
    void getReviewsByProductId_withNoReviews_returnsEmptyList() {
        when(reviewService.getReviewsByProductId(productId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ReviewDto>> response = reviewController.getReviewsByProductId(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(reviewService, times(1)).getReviewsByProductId(productId);
    }

    @Test
    void getReviewsByProductId_withSingleReview_returnsSingleReview() {
        ReviewDto review = new ReviewDto();
        review.setReviewId(1L);
        review.setUsername("solo_user");
        review.setContent("First review!");
        review.setRating(3);
        review.setProfilePictureUrl("https://example.com/solo.jpg");

        List<ReviewDto> reviews = Collections.singletonList(review);

        when(reviewService.getReviewsByProductId(productId)).thenReturn(reviews);

        ResponseEntity<List<ReviewDto>> response = reviewController.getReviewsByProductId(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("solo_user", response.getBody().getFirst().getUsername());
        assertEquals("First review!", response.getBody().getFirst().getContent());
        assertEquals(3, response.getBody().getFirst().getRating());
        verify(reviewService, times(1)).getReviewsByProductId(productId);
    }

    @Test
    void createReview_withLongComment_returnsCreatedResponse() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        String longComment = "This is a very detailed review. ".repeat(50); // Long but valid comment
        dto.setComment(longComment);
        dto.setRating(4);

        String username = "detailed_reviewer";
        when(authentication.getName()).thenReturn(username);
        when(reviewService.createReview(eq(productId), eq(dto), eq(username))).thenReturn(reviewId);

        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyLong())).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/review/" + reviewId));
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = reviewController.createReview(productId, dto, authentication);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Successfully created review", response.getBody().getMessage());
            verify(reviewService, times(1)).createReview(eq(productId), eq(dto), eq(username));
        }
    }

    @Test
    void getReviewsByProductId_withMultipleReviews_returnsAllReviews() {
        ReviewDto review1 = new ReviewDto(1L, "user1", "url1", "Comment 1", 5);
        ReviewDto review2 = new ReviewDto(2L, "user2", "url2", "Comment 2", 4);
        ReviewDto review3 = new ReviewDto(3L, "user3", "url3", "Comment 3", 3);

        List<ReviewDto> reviews = Arrays.asList(review1, review2, review3);

        when(reviewService.getReviewsByProductId(productId)).thenReturn(reviews);

        ResponseEntity<List<ReviewDto>> response = reviewController.getReviewsByProductId(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(reviewService, times(1)).getReviewsByProductId(productId);
    }
}
