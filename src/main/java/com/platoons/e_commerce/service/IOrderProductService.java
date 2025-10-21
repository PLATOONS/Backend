package com.platoons.e_commerce.service;

import com.platoons.e_commerce.dto.AddToCartRequestDto;

public interface IOrderProductService {

    void addToCart(AddToCartRequestDto request, String userId);

    void removeFromCart(String productId, String userId);
}
