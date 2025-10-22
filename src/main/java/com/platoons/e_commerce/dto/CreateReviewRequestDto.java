package com.platoons.e_commerce.dto;

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
public class CreateReviewRequestDto {
    @NotBlank(message = "comment is required")
    @Size(max = 4000, message = "comment is too long")
    @Column(nullable = false, length = 4000)
    private String comment;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;
}
