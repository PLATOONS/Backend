package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.service.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final IReviewService reviewService;

    @PostMapping("/{productId}")
    public ResponseEntity<GenericResponseDto> createReview(
            @PathVariable String productId,
            @Valid @RequestBody CreateReviewRequestDto reviewRequestDto,
            Authentication authentication) {
        log.info("Creating review");
        Long reviewId = reviewService.createReview(productId, reviewRequestDto, authentication.getName());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/review/{id}")
                .buildAndExpand(reviewId)
                .toUri();

        log.info("Review created with id {}", reviewId);
        return ResponseEntity.created(uri)
                .body(new GenericResponseDto("Successfully created review"));
    }
}
