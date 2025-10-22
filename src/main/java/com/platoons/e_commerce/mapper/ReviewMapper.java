package com.platoons.e_commerce.mapper;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.entity.Review;

public class ReviewMapper {
    public static Review mapCreateReviewRequestDtoToReview(CreateReviewRequestDto reviewDto, Review review) {
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        return review;
    }
}
