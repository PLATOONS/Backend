package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequestDto(
        @Schema(description = "Product identifier", example = "PROD-123")
        @NotBlank
        String productId,
        @Schema(description = "Quantity to add (>=1)", example = "2")
        @NotNull
        @Min(1)
        Integer quantity,
        @Schema(description = "Selected color (nullable)", example = "red", nullable = true)
        String color
        ) {

}
