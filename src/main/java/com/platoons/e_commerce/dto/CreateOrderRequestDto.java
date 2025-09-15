package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateOrderRequestDto {

    @Schema(
            description = "The ID of the order",
            example = "1"
    )
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @Schema(
            description = "The ID of the customer making the order",
            example = "b05ac192-6054-4696-b741-e471e6e472ec"
    )
    @NotNull(message = "Customer ID is required")
    private String customer;

    @Schema(
            description = "The ID of the coupon being used",
            example = "1"
    )
    private Long couponId;

    @Schema(
            description = "Subtotal amount before discounts",
            example = "100.00",
            minimum = "0.0"
    )
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", message = "Sub total must be positive")
    private Double subTotalAmount;

    @Schema(
        description = "Total amount after discounts",
        example = "107.99",
        minimum = "0.0"
    )
    @NotNull(message = "Total is required")
    @DecimalMin(value = "0.0", message = "Total must be positive")
    private Double totalAmout;
}
