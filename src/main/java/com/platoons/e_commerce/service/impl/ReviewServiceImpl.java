package com.platoons.e_commerce.service.impl;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.dto.ReviewDto;
import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.entity.OrderProduct;
import com.platoons.e_commerce.entity.Review;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.exceptions.NotBoughtException;
import com.platoons.e_commerce.mapper.ReviewMapper;
import com.platoons.e_commerce.repository.OrderProductRepository;
import com.platoons.e_commerce.repository.ProductRepository;
import com.platoons.e_commerce.repository.ReviewRepository;
import com.platoons.e_commerce.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final OrderProductRepository orderProductRepository;

    @Override
    public Long createReview(String productId, CreateReviewRequestDto reviewDto, String username) {
        // check if user has bought the product
        boolean userBoughtProduct = productRepository.userBoughtProduct(username, productId);

        if(!userBoughtProduct)
            throw new NotBoughtException("User has not purchased this product");

        // get the user and order product to save the review
        OrderProduct orderProduct = orderProductRepository.findFirstByProductProductIdAndOrderCustomerUsernameAndOrderOrderStatusStatusNameAndDeletedAtIsNull(productId, username, "COMPLETED")
                .orElseThrow(() -> new NotBoughtException("User has not purchased this product"));

        Customer customer = orderProduct.getOrder().getCustomer();

        Review review = ReviewMapper.mapCreateReviewRequestDtoToReview(reviewDto, new Review());

        review.setCustomer(customer);
        review.setOrderProduct(orderProduct);

        Review savedReview = reviewRepository.save(review);

        return savedReview.getReviewId();
    }

    @Override
    public List<ReviewDto> getReviewsByProductId(String productId) {
        List<Review> reviews = reviewRepository.findByOrderProductProductProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("product", "productId", productId));

        List<ReviewDto> ans = new ArrayList<>();

        reviews.forEach(review -> ans.add(ReviewMapper.mapReviewToReviewDto(review, new ReviewDto())));

        return ans;
    }
}
