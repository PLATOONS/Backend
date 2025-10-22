package com.platoons.e_commerce.service;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;

public interface IReviewService {
    Long createReview(String productId, CreateReviewRequestDto reviewDto, String username);
}
