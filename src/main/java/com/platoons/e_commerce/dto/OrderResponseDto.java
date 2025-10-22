package com.platoons.e_commerce.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private String status;
    private double subtotalAmount;
    private double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderProductDto> products;
    private String customerId;
    private String customerUsername;
    private String customerEmail;
    private Long couponId;
    private String couponCode;
    private Double discountAmount;
}
