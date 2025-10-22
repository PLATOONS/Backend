package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.CreateReviewRequestDto;
import com.platoons.e_commerce.dto.ErrorResponseDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.dto.ReviewDto;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.exceptions.NotBoughtException;
import com.platoons.e_commerce.service.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/review")
@Tag(name = "Review Controller", description = "APIs for managing product reviews")
public class ReviewController {

    private final IReviewService reviewService;

    @Operation(summary = "Create a new review", description = "Creates a new review for a specific product. Requires authentication.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created successfully",
                     content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "User has not purchased this product",
                     content = @Content(schema = @Schema(implementation = NotBoughtException.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponseDto> createReview(
            @Parameter(description = "ID of the product to review", required = true)
            @PathVariable String productId,
            @Parameter(description = "Review details including comment and rating", required = true)
            @Valid @RequestBody CreateReviewRequestDto reviewRequestDto,
            @Parameter(hidden = true) Authentication authentication) {
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

    @Operation(summary = "Get reviews by product ID", description = "Retrieves all reviews for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews",
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewDto.class)))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = EntityNotFoundException.class)))
    })
    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReviewDto>> getReviewsByProductId(
            @Parameter(description = "ID of the product to get reviews for", required = true)
            @PathVariable String productId) {
        log.info("Getting reviews by product id {}", productId);
        List<ReviewDto> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }
}
