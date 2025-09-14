package com.platoons.e_commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatus {

    @NotNull(message = "Order Status Id is required")
    private Long statusId;

    @NotBlank(message = "Status name is required")
    private String statusName;
}
