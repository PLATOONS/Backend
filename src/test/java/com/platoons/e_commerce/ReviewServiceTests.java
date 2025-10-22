package com.platoons.e_commerce;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.dto.ReviewDto;
import com.platoons.e_commerce.entity.*;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.exceptions.NotBoughtException;
import com.platoons.e_commerce.repository.OrderProductRepository;
import com.platoons.e_commerce.repository.ProductRepository;
import com.platoons.e_commerce.repository.ReviewRepository;
import com.platoons.e_commerce.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReviewServiceTests {

    private ProductRepository productRepository;
    private ReviewRepository reviewRepository;
    private OrderProductRepository orderProductRepository;
    private ReviewServiceImpl reviewService;

    private String productId;
    private String username;
    private CreateReviewRequestDto createReviewDto;
    private Customer customer;
    private OrderProduct orderProduct;
    private Review review;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        orderProductRepository = mock(OrderProductRepository.class);
        reviewService = new ReviewServiceImpl(productRepository, reviewRepository, orderProductRepository);

        productId = UUID.randomUUID().toString();
        username = "john_doe";

        createReviewDto = new CreateReviewRequestDto();
        createReviewDto.setComment("Great product! Highly recommended.");
        createReviewDto.setRating(5);

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID().toString());
        customer.setUsername(username);
        customer.setEmail("john@example.com");
        customer.setFirstName("John");
        customer.setLastName("Doe");

        product = new Product();
        product.setProductId(productId);

        order = new Order();
        order.setOrderId(1L);
        order.setCustomer(customer);

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatusName("COMPLETED");
        order.setOrderStatus(orderStatus);

        orderProduct = new OrderProduct();
        orderProduct.setOrderProductId(1L);
        orderProduct.setProduct(product);
        orderProduct.setOrder(order);
        orderProduct.setQuantity(2);
        orderProduct.setTotalPrice(100.0);

        review = new Review();
        review.setReviewId(1L);
        review.setComment(createReviewDto.getComment());
        review.setRating(createReviewDto.getRating());
        review.setCustomer(customer);
        review.setOrderProduct(orderProduct);
    }

    @Test
    void createReview_withValidData_returnsReviewId() {
        when(productRepository.userBoughtProduct(username, productId)).thenReturn(true);
        when(orderProductRepository.findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, username, "COMPLETED")).thenReturn(Optional.of(orderProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Long reviewId = reviewService.createReview(productId, createReviewDto, username);

        assertNotNull(reviewId);
        assertEquals(1L, reviewId);
        verify(productRepository, times(1)).userBoughtProduct(username, productId);
        verify(orderProductRepository, times(1)).findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, username, "COMPLETED");
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_whenUserHasNotBoughtProduct_throwsNotBoughtException() {
        when(productRepository.userBoughtProduct(username, productId)).thenReturn(false);

        NotBoughtException exception = assertThrows(NotBoughtException.class, () ->
                reviewService.createReview(productId, createReviewDto, username)
        );

        assertEquals("User has not purchased this product", exception.getMessage());
        verify(productRepository, times(1)).userBoughtProduct(username, productId);
        verify(orderProductRepository, never()).findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                anyString(), anyString(), anyString());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_whenOrderProductNotFound_throwsNotBoughtException() {
        when(productRepository.userBoughtProduct(username, productId)).thenReturn(true);
        when(orderProductRepository.findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, username, "COMPLETED")).thenReturn(Optional.empty());

        NotBoughtException exception = assertThrows(NotBoughtException.class, () ->
                reviewService.createReview(productId, createReviewDto, username)
        );

        assertEquals("User has not purchased this product", exception.getMessage());
        verify(productRepository, times(1)).userBoughtProduct(username, productId);
        verify(orderProductRepository, times(1)).findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, username, "COMPLETED");
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_withMinimumRating_returnsReviewId() {
        createReviewDto.setRating(1);
        createReviewDto.setComment("Not satisfied with the product.");
        review.setRating(1);
        review.setComment("Not satisfied with the product.");

        when(productRepository.userBoughtProduct(username, productId)).thenReturn(true);
        when(orderProductRepository.findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, username, "COMPLETED")).thenReturn(Optional.of(orderProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Long reviewId = reviewService.createReview(productId, createReviewDto, username);

        assertNotNull(reviewId);
        assertEquals(1L, reviewId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_withMaximumRating_returnsReviewId() {
        createReviewDto.setRating(5);
        createReviewDto.setComment("Absolutely perfect! Best purchase ever.");
        review.setRating(5);
        review.setComment("Absolutely perfect! Best purchase ever.");

        when(productRepository.userBoughtProduct(username, productId)).thenReturn(true);
        when(orderProductRepository.findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, username, "COMPLETED")).thenReturn(Optional.of(orderProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Long reviewId = reviewService.createReview(productId, createReviewDto, username);

        assertNotNull(reviewId);
        assertEquals(1L, reviewId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_withLongComment_returnsReviewId() {
        String longComment = "This is a very detailed review. ".repeat(50);
        createReviewDto.setComment(longComment);
        review.setComment(longComment);

        when(productRepository.userBoughtProduct(username, productId)).thenReturn(true);
        when(orderProductRepository.findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, username, "COMPLETED")).thenReturn(Optional.of(orderProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Long reviewId = reviewService.createReview(productId, createReviewDto, username);

        assertNotNull(reviewId);
        assertEquals(1L, reviewId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_savesReviewWithCorrectData() {
        when(productRepository.userBoughtProduct(username, productId)).thenReturn(true);
        when(orderProductRepository.findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, username, "COMPLETED")).thenReturn(Optional.of(orderProduct));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            assertEquals(createReviewDto.getComment(), savedReview.getComment());
            assertEquals(createReviewDto.getRating(), savedReview.getRating());
            assertEquals(customer, savedReview.getCustomer());
            assertEquals(orderProduct, savedReview.getOrderProduct());
            savedReview.setReviewId(1L);
            return savedReview;
        });

        Long reviewId = reviewService.createReview(productId, createReviewDto, username);

        assertNotNull(reviewId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void getReviewsByProductId_withMultipleReviews_returnsListOfReviewDtos() {
        Review review1 = new Review();
        review1.setReviewId(1L);
        review1.setComment("Great product!");
        review1.setRating(5);
        review1.setCustomer(customer);
        review1.setOrderProduct(orderProduct);

        Customer customer2 = new Customer();
        customer2.setCustomerId(UUID.randomUUID().toString());
        customer2.setUsername("jane_doe");

        Review review2 = new Review();
        review2.setReviewId(2L);
        review2.setComment("Good quality.");
        review2.setRating(4);
        review2.setCustomer(customer2);
        review2.setOrderProduct(orderProduct);

        List<Review> reviews = Arrays.asList(review1, review2);

        when(reviewRepository.findByOrderProductProductProductId(productId)).thenReturn(Optional.of(reviews));

        List<ReviewDto> result = reviewService.getReviewsByProductId(productId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getReviewId());
        assertEquals("Great product!", result.get(0).getContent());
        assertEquals(5, result.get(0).getRating());
        assertEquals("john_doe", result.get(0).getUsername());
        assertEquals(2L, result.get(1).getReviewId());
        assertEquals("Good quality.", result.get(1).getContent());
        assertEquals(4, result.get(1).getRating());
        assertEquals("jane_doe", result.get(1).getUsername());
        verify(reviewRepository, times(1)).findByOrderProductProductProductId(productId);
    }

    @Test
    void getReviewsByProductId_withSingleReview_returnsSingleReviewDto() {
        List<Review> reviews = Collections.singletonList(review);

        when(reviewRepository.findByOrderProductProductProductId(productId)).thenReturn(Optional.of(reviews));

        List<ReviewDto> result = reviewService.getReviewsByProductId(productId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getReviewId());
        assertEquals("Great product! Highly recommended.", result.get(0).getContent());
        assertEquals(5, result.get(0).getRating());
        assertEquals("john_doe", result.get(0).getUsername());
        verify(reviewRepository, times(1)).findByOrderProductProductProductId(productId);
    }

    @Test
    void getReviewsByProductId_withEmptyList_returnsEmptyList() {
        List<Review> reviews = Collections.emptyList();

        when(reviewRepository.findByOrderProductProductProductId(productId)).thenReturn(Optional.of(reviews));

        List<ReviewDto> result = reviewService.getReviewsByProductId(productId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewRepository, times(1)).findByOrderProductProductProductId(productId);
    }

    @Test
    void getReviewsByProductId_whenProductNotFound_throwsEntityNotFoundException() {
        when(reviewRepository.findByOrderProductProductProductId(productId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                reviewService.getReviewsByProductId(productId)
        );

        assertTrue(exception.getMessage().contains("product"));
        assertTrue(exception.getMessage().contains("productId"));
        assertTrue(exception.getMessage().contains(productId));
        verify(reviewRepository, times(1)).findByOrderProductProductProductId(productId);
    }

    @Test
    void getReviewsByProductId_withDifferentRatings_returnsAllReviews() {
        Review review1 = new Review();
        review1.setReviewId(1L);
        review1.setComment("Excellent!");
        review1.setRating(5);
        review1.setCustomer(customer);
        review1.setOrderProduct(orderProduct);

        Customer customer2 = new Customer();
        customer2.setCustomerId(UUID.randomUUID().toString());
        customer2.setUsername("user2");

        Review review2 = new Review();
        review2.setReviewId(2L);
        review2.setComment("Average");
        review2.setRating(3);
        review2.setCustomer(customer2);
        review2.setOrderProduct(orderProduct);

        Customer customer3 = new Customer();
        customer3.setCustomerId(UUID.randomUUID().toString());
        customer3.setUsername("user3");

        Review review3 = new Review();
        review3.setReviewId(3L);
        review3.setComment("Poor quality");
        review3.setRating(1);
        review3.setCustomer(customer3);
        review3.setOrderProduct(orderProduct);

        List<Review> reviews = Arrays.asList(review1, review2, review3);

        when(reviewRepository.findByOrderProductProductProductId(productId)).thenReturn(Optional.of(reviews));

        List<ReviewDto> result = reviewService.getReviewsByProductId(productId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(5, result.get(0).getRating());
        assertEquals(3, result.get(1).getRating());
        assertEquals(1, result.get(2).getRating());
        verify(reviewRepository, times(1)).findByOrderProductProductProductId(productId);
    }

    @Test
    void createReview_withDifferentUsername_returnsReviewId() {
        String differentUsername = "different_user";
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(UUID.randomUUID().toString());
        differentCustomer.setUsername(differentUsername);
        differentCustomer.setEmail("different@example.com");
        differentCustomer.setFirstName("Different");
        differentCustomer.setLastName("User");

        Order differentOrder = new Order();
        differentOrder.setOrderId(2L);
        differentOrder.setCustomer(differentCustomer);

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatusName("COMPLETED");
        differentOrder.setOrderStatus(orderStatus);

        OrderProduct differentOrderProduct = new OrderProduct();
        differentOrderProduct.setOrderProductId(2L);
        differentOrderProduct.setProduct(product);
        differentOrderProduct.setOrder(differentOrder);

        Review differentReview = new Review();
        differentReview.setReviewId(2L);
        differentReview.setComment(createReviewDto.getComment());
        differentReview.setRating(createReviewDto.getRating());
        differentReview.setCustomer(differentCustomer);
        differentReview.setOrderProduct(differentOrderProduct);

        when(productRepository.userBoughtProduct(differentUsername, productId)).thenReturn(true);
        when(orderProductRepository.findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, differentUsername, "COMPLETED")).thenReturn(Optional.of(differentOrderProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(differentReview);

        Long reviewId = reviewService.createReview(productId, createReviewDto, differentUsername);

        assertNotNull(reviewId);
        assertEquals(2L, reviewId);
        verify(productRepository, times(1)).userBoughtProduct(differentUsername, productId);
        verify(orderProductRepository, times(1)).findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(
                productId, differentUsername, "COMPLETED");
        verify(reviewRepository, times(1)).save(any(Review.class));
    }
}
