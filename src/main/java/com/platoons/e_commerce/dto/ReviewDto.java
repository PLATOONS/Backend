package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Review information")
public class ReviewDto {
    @Schema(description = "Unique identifier of the review", example = "1")
    private Long reviewId;
    
    @Schema(description = "Username of the reviewer", example = "john_doe")
    private String username;
    
    @Schema(description = "URL of the reviewer's profile picture", example = "https://example.com/profile.jpg")
    private String profilePictureUrl;
    
    @Schema(description = "Review comment content", example = "Great product! Highly recommended.")
    private String content;
    
    @Schema(description = "Rating given by the reviewer (1-5)", example = "5", minimum = "1", maximum = "5")
    private int rating;
}
