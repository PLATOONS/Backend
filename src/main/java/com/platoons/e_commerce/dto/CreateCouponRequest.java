package com.platoons.e_commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Payload to create a coupon")
public class CreateCouponRequest {

    @JsonProperty("disscount_code") // nombre EXACTO pedido por contrato
    @Schema(description = "Coupon code (unique, 3-20 chars)", example = "WELCOME10")
    @NotNull @NotBlank
    @Size(min = 3, max = 20, message = "Coupon code must be between 3 and 20 characters long")
    private String couponCode;

    @JsonProperty("disscount_amount") // porcentaje (0–100)
    @Schema(description = "Discount percentage", example = "10.0", minimum = "0.01", maximum = "100.0")
    @NotNull
    @DecimalMin(value = "0.01", message = "Discount amount must be at least 0.01")
    @DecimalMax(value = "100.00", message = "Discount amount must not exceed 100.00")
    private Double discountAmount;
}

