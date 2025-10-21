package com.platoons.e_commerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateQuantityRequestDto {
    @NotBlank
    private String productId;

    @Min(1)
    private int quantity;
}
