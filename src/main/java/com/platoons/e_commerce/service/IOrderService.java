package com.platoons.e_commerce.service;

import java.util.List;
import com.platoons.e_commerce.dto.*;

public interface IOrderService {
    String createOrder(CreateOrderRequestDto orderDto);

    OrderResponseDto fetchOrder(String orderId);

    void deleteOrder(String orderId);

    String updateOrder(UpdateOrderDto orderDto, String orderId);

    List<OrderResponseDto> getOrdersByUser(String username);
}
