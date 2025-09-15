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
@Schema(description = "Order details")
public class OrderDto {

    @Schema(
            description = "ID of the order",
            example = "1"
    )
    private Long orderId;

    @Schema(
            description = "Subtotal amount of the order",
            example = "100.00"
    )
    private Double subTotalAmout;

    @Schema(
            description = "Total amount of the order",
            example = "100.00"
    )
    private Double totalAmount;

    @Schema(
        description = "Customer who placed the order"
    )
    private CustomerDto customer;

    @Schema(
        description = "Coupon applied to the order, if any"
    )
    private CouponDto coupon;
}
