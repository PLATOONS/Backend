package com.platoons.e_commerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderProduct {

    @NotNull(message = "OrderProduct ID is requiered")
    private Long orderProductId;

    @NotNull(message = "Product ID is required")
    private Long product;

    @NotNull(message = "Order Id is required")
    private Long Order;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Total price is required")
    private Double totalPrice;
}
