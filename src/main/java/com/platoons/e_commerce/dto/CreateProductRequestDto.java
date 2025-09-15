package com.platoons.e_commerce.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Product creation request")
public class CreateProductRequestDto {
    @Schema(
            description = "The name of the product",
            example = "Product Name"
    )
    @NotNull(message = "Name is required")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(
            description = "The discount of the product",
            example = "10.00"
    )
    private double discount;

    @Schema(
            description = "The measurements of the product",
            example = "10 x 10 x 10"
    )
    private String measurements;

    @Schema(
            description = "The description of the product",
            example = "This is a product description"
    )
    @NotNull(message = "Description is required")
    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Schema(
            description = "The price of the product",
            example = "100.00"
    )
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than 0")
    private double price;

    @Schema(
            description = "The amount of stock available for the product",
            example = "5"
    )
    @NotNull(message = "Stock quantity is required")
    private int stockQuantity;

    @Schema(
            description = "The ID of the category of the product",
            example = "1"
    )
    @NotNull(message = "Category is required")
    private Long categoryId;

    @Schema(
        description = "List of available colors for the product",
        example = "[\"Red\", \"Blue\", \"Black\"]"
    )
    private List<String> colors;
}
