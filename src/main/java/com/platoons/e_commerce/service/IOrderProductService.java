package com.platoons.e_commerce.service;

import com.platoons.e_commerce.dto.AddToCartRequestDto;

public interface IOrderProductService {

    void addToCart(AddToCartRequestDto request, String username);

    void removeFromCart(String productId, String username);
    
    void updateQuantity(String productId, int quantity, String username);
}
