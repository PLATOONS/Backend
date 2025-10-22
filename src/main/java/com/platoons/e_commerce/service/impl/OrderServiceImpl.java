package com.platoons.e_commerce.service.impl;

import com.platoons.e_commerce.dto.CreateOrderRequestDto;
import com.platoons.e_commerce.dto.OrderDto;
import com.platoons.e_commerce.dto.UpdateOrderDto;
import com.platoons.e_commerce.entity.Order;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.OrderMapper;
import com.platoons.e_commerce.repository.OrderRepository;
import com.platoons.e_commerce.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;

    @Override
    public String createOrder(CreateOrderRequestDto orderDto) {
        Order order = OrderMapper.mapCreateOrderRequestDtoToOrder(orderDto, new Order());
        var savedOrder = orderRepository.save(order);
        return savedOrder.getOrderId().toString();
    }

    @Override
    public OrderDto fetchOrder(String orderId) {
        var savedOrder = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new EntityNotFoundException("order", "orderId", orderId));
        return OrderMapper.mapOrderToOrderDto(savedOrder, new OrderDto());
    }

    @Override
    public String updateOrder(UpdateOrderDto orderDto, String orderId) {
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new EntityNotFoundException("order", "orderId", orderId));

        OrderMapper.mapUpdateOrderDtoToOrder(orderDto, order);
        orderRepository.save(order);
        return order.getOrderId().toString();
    }

    @Override
    public void deleteOrder(String orderId) {
        var optionalOrder = orderRepository.findById(Long.parseLong(orderId));
        if (optionalOrder.isEmpty())
            return;

        Order order = optionalOrder.get();
        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public List<OrderDto> getOrdersByUser(String username) {
        var orders = orderRepository.findAllByCustomerUsernameAndDeletedAtIsNull(username);

        return orders.stream()
                .map(order -> OrderMapper.mapOrderToOrderDto(order, new OrderDto()))
                .collect(Collectors.toList());
    }
}
