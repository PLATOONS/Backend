package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Product image details")
public class ProductImageDto {
    @Schema(
            description = "Color of the product variant",
            example = "Blue"
    )
    private String color;

    @Schema(
        description = "URL to the product image",
        example = "https://example.com/images/blue-tshirt.jpg",
        format = "uri"
    )
    private String imageUrl;
}
