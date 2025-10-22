package com.platoons.e_commerce.service;

import com.platoons.e_commerce.dto.CreateOrderRequestDto;
import com.platoons.e_commerce.dto.OrderDto;
import com.platoons.e_commerce.dto.UpdateOrderDto;

public interface IOrderService {
    String createOrder(CreateOrderRequestDto orderDto);

    OrderDto fetchOrder(String orderId);

    void deleteOrder(String orderId);

    String updateOrder(UpdateOrderDto orderDto, String orderId);

    List<OrderResponseDto> getOrdersByUser(String username);
}
