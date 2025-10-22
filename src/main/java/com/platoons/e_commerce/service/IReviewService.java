package com.platoons.e_commerce.service;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.dto.ReviewDto;

import java.util.List;

public interface IReviewService {
    Long createReview(String productId, CreateReviewRequestDto reviewDto, String username);
    List<ReviewDto> getReviewsByProductId(String productId);
}
