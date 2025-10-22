package com.platoons.e_commerce.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CartProductsDto {
    private String productId;
    private String productName;
    private double price;
    private String imageUrl;
    private String color;
    private int quantity;
}
