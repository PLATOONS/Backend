package com.platoons.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductSummaryDto {
    private String productId;
    private double price;
    private double discountPercentage;
    private double discountedPrice;
    private double ratingAverage;
    private boolean wishlisted;
    private String name;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String categoryName;
    private long reviewCount;
}
