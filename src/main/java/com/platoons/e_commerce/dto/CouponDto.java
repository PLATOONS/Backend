package com.platoons.e_commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Coupon details")
public class CouponDto {

    @JsonProperty("disscount_id")   // ← nombre JSON requerido
    @Schema(description = "ID of the used coupon", example = "1")
    private Long couponId;

    @JsonProperty("disscount_code") // ← nombre JSON requerido
    @Schema(description = "Code used to identify the coupon", example = "SAVE20")
    private String couponCode;

    @JsonProperty("disscount_amount") // ← nombre JSON requerido (porcentaje 0–100)
    @Schema(description = "The amount or percentage of the discount", example = "25.0", minimum = "0.0")
    private Double discountAmount;
}
