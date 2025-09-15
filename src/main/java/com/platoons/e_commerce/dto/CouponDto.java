package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Coupon details")
public class CouponDto {

    @Schema(
            description = "ID of the used coupon",
            example = "1"
    )
    private Long couponId;

    @Schema(
            description = "Code used to identify the coupon",
            example = "SAVE20"
    )
    private String couponCode;

    @Schema(
        description = "The amount or percentage of the discount",
        example = "25.0",
        minimum = "0.0"
    )
    private Double discountAmount;
}
