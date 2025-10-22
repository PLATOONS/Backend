package com.platoons.e_commerce.mapper;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.dto.ReviewDto;
import com.platoons.e_commerce.entity.Review;

public class ReviewMapper {
    public static Review mapCreateReviewRequestDtoToReview(CreateReviewRequestDto reviewDto, Review review) {
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        return review;
    }

    public static ReviewDto mapReviewToReviewDto(Review review, ReviewDto reviewDto) {
        reviewDto.setReviewId(review.getReviewId());
        reviewDto.setContent(review.getComment());
        reviewDto.setRating(review.getRating());
        reviewDto.setUsername(review.getCustomer().getUsername());
        return reviewDto;
    }
}
