package com.platoons.e_commerce;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.dto.ReviewDto;
import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.entity.OrderProduct;
import com.platoons.e_commerce.entity.Review;
import com.platoons.e_commerce.mapper.ReviewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReviewMapperTests {

    private Review review;
    private Customer customer;
    private OrderProduct orderProduct;

    @BeforeEach
    void setUp() {
        customer = mock(Customer.class);
        orderProduct = mock(OrderProduct.class);
        review = new Review();
    }

    @Test
    void mapCreateReviewRequestDtoToReview_mapsAllFields() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        dto.setComment("Great product! Highly recommended.");
        dto.setRating(5);

        Review result = ReviewMapper.mapCreateReviewRequestDtoToReview(dto, review);

        assertNotNull(result);
        assertEquals("Great product! Highly recommended.", result.getComment());
        assertEquals(5, result.getRating());
        assertSame(review, result);
    }

    @Test
    void mapCreateReviewRequestDtoToReview_withMinimumRating_mapsCorrectly() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        dto.setComment("Not satisfied with the product.");
        dto.setRating(1);

        Review result = ReviewMapper.mapCreateReviewRequestDtoToReview(dto, review);

        assertNotNull(result);
        assertEquals("Not satisfied with the product.", result.getComment());
        assertEquals(1, result.getRating());
    }

    @Test
    void mapCreateReviewRequestDtoToReview_withMaximumRating_mapsCorrectly() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        dto.setComment("Absolutely perfect! Best purchase ever.");
        dto.setRating(5);

        Review result = ReviewMapper.mapCreateReviewRequestDtoToReview(dto, review);

        assertNotNull(result);
        assertEquals("Absolutely perfect! Best purchase ever.", result.getComment());
        assertEquals(5, result.getRating());
    }

    @Test
    void mapCreateReviewRequestDtoToReview_withLongComment_mapsCorrectly() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        String longComment = "This is a very detailed review. ".repeat(50);
        dto.setComment(longComment);
        dto.setRating(4);

        Review result = ReviewMapper.mapCreateReviewRequestDtoToReview(dto, review);

        assertNotNull(result);
        assertEquals(longComment, result.getComment());
        assertEquals(4, result.getRating());
    }

    @Test
    void mapCreateReviewRequestDtoToReview_updatesExistingReview() {
        review.setReviewId(1L);
        review.setComment("Old comment");
        review.setRating(3);
        review.setCustomer(customer);
        review.setOrderProduct(orderProduct);

        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        dto.setComment("Updated comment");
        dto.setRating(5);

        Review result = ReviewMapper.mapCreateReviewRequestDtoToReview(dto, review);

        assertNotNull(result);
        assertEquals("Updated comment", result.getComment());
        assertEquals(5, result.getRating());
        assertEquals(1L, result.getReviewId());
        assertSame(customer, result.getCustomer());
        assertSame(orderProduct, result.getOrderProduct());
    }

    @Test
    void mapReviewToReviewDto_mapsAllFields() {
        review.setReviewId(1L);
        review.setComment("Great product!");
        review.setRating(5);
        review.setCustomer(customer);

        when(customer.getUsername()).thenReturn("john_doe");

        ReviewDto dto = new ReviewDto();
        ReviewDto result = ReviewMapper.mapReviewToReviewDto(review, dto);

        assertNotNull(result);
        assertEquals(1L, result.getReviewId());
        assertEquals("Great product!", result.getContent());
        assertEquals(5, result.getRating());
        assertEquals("john_doe", result.getUsername());
        assertSame(dto, result);
    }

    @Test
    void mapReviewToReviewDto_withDifferentUsername_mapsCorrectly() {
        review.setReviewId(2L);
        review.setComment("Good quality product.");
        review.setRating(4);
        review.setCustomer(customer);

        when(customer.getUsername()).thenReturn("jane_smith");

        ReviewDto dto = new ReviewDto();
        ReviewDto result = ReviewMapper.mapReviewToReviewDto(review, dto);

        assertNotNull(result);
        assertEquals(2L, result.getReviewId());
        assertEquals("Good quality product.", result.getContent());
        assertEquals(4, result.getRating());
        assertEquals("jane_smith", result.getUsername());
    }

    @Test
    void mapReviewToReviewDto_withMinimumRating_mapsCorrectly() {
        review.setReviewId(3L);
        review.setComment("Disappointing experience.");
        review.setRating(1);
        review.setCustomer(customer);

        when(customer.getUsername()).thenReturn("test_user");

        ReviewDto dto = new ReviewDto();
        ReviewDto result = ReviewMapper.mapReviewToReviewDto(review, dto);

        assertNotNull(result);
        assertEquals(3L, result.getReviewId());
        assertEquals("Disappointing experience.", result.getContent());
        assertEquals(1, result.getRating());
        assertEquals("test_user", result.getUsername());
    }

    @Test
    void mapReviewToReviewDto_withMaximumRating_mapsCorrectly() {
        review.setReviewId(4L);
        review.setComment("Perfect in every way!");
        review.setRating(5);
        review.setCustomer(customer);

        when(customer.getUsername()).thenReturn("happy_customer");

        ReviewDto dto = new ReviewDto();
        ReviewDto result = ReviewMapper.mapReviewToReviewDto(review, dto);

        assertNotNull(result);
        assertEquals(4L, result.getReviewId());
        assertEquals("Perfect in every way!", result.getContent());
        assertEquals(5, result.getRating());
        assertEquals("happy_customer", result.getUsername());
    }

    @Test
    void mapReviewToReviewDto_updatesExistingDto() {
        review.setReviewId(5L);
        review.setComment("New review comment");
        review.setRating(3);
        review.setCustomer(customer);

        when(customer.getUsername()).thenReturn("updated_user");

        ReviewDto dto = new ReviewDto();
        dto.setReviewId(999L);
        dto.setContent("Old content");
        dto.setRating(1);
        dto.setUsername("old_user");
        dto.setProfilePictureUrl("old_url");

        ReviewDto result = ReviewMapper.mapReviewToReviewDto(review, dto);

        assertNotNull(result);
        assertEquals(5L, result.getReviewId());
        assertEquals("New review comment", result.getContent());
        assertEquals(3, result.getRating());
        assertEquals("updated_user", result.getUsername());
        assertEquals("old_url", result.getProfilePictureUrl());
        assertSame(dto, result);
    }

    @Test
    void mapReviewToReviewDto_withLongComment_mapsCorrectly() {
        String longComment = "This is a very detailed review with lots of information. ".repeat(30);
        review.setReviewId(6L);
        review.setComment(longComment);
        review.setRating(4);
        review.setCustomer(customer);

        when(customer.getUsername()).thenReturn("detailed_reviewer");

        ReviewDto dto = new ReviewDto();
        ReviewDto result = ReviewMapper.mapReviewToReviewDto(review, dto);

        assertNotNull(result);
        assertEquals(6L, result.getReviewId());
        assertEquals(longComment, result.getContent());
        assertEquals(4, result.getRating());
        assertEquals("detailed_reviewer", result.getUsername());
    }

    @Test
    void mapCreateReviewRequestDtoToReview_withMidRangeRating_mapsCorrectly() {
        CreateReviewRequestDto dto = new CreateReviewRequestDto();
        dto.setComment("Average product, meets expectations.");
        dto.setRating(3);

        Review result = ReviewMapper.mapCreateReviewRequestDtoToReview(dto, review);

        assertNotNull(result);
        assertEquals("Average product, meets expectations.", result.getComment());
        assertEquals(3, result.getRating());
    }

    @Test
    void mapReviewToReviewDto_preservesProfilePictureUrl() {
        review.setReviewId(7L);
        review.setComment("Test comment");
        review.setRating(5);
        review.setCustomer(customer);

        when(customer.getUsername()).thenReturn("test_user");

        ReviewDto dto = new ReviewDto();
        dto.setProfilePictureUrl("https://example.com/profile.jpg");

        ReviewDto result = ReviewMapper.mapReviewToReviewDto(review, dto);

        assertNotNull(result);
        assertEquals("https://example.com/profile.jpg", result.getProfilePictureUrl());
    }
}
