package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Request object for creating a new review")
public class CreateReviewRequestDto {
    @Schema(description = "Review comment text", 
            example = "This product exceeded my expectations. The quality is outstanding!",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 4000)
    @NotBlank(message = "comment is required")
    @Size(max = 4000, message = "comment is too long")
    @Column(nullable = false, length = 4000)
    private String comment;

    @Schema(description = "Rating value from 1 to 5", 
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1",
            maximum = "5")
    @NotNull(message = "rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;
}
