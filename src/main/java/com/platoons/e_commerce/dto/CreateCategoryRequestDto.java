package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCategoryRequestDto {
    @Schema(
            description = "Category name",
            example = "Electronics"
    )
    @NotNull(message = "Category name is required")
    @NotBlank(message = "Category name cannot be blank")
    private String name;

    @Schema(
            description = "Category description",
            example = "Electronics"
    )
    @NotNull(message = "Description is required")
    @NotBlank(message = "Description is required")
    private String description;

    private String test;
}
