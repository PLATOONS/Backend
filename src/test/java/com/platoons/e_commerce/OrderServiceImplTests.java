package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.platoons.e_commerce.dto.CreateOrderRequestDto;
import com.platoons.e_commerce.dto.OrderDto;
import com.platoons.e_commerce.dto.UpdateOrderDto;
import com.platoons.e_commerce.entity.Order;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.mapper.OrderMapper;
import com.platoons.e_commerce.repository.OrderRepository;
import com.platoons.e_commerce.service.impl.OrderServiceImpl;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class OrderServiceImplTests {
    private OrderRepository orderRepository;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderServiceImpl(orderRepository);
    }

    @Test
    void testCreateOrder() {
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        Order order = new Order();
        order.setOrderId(1L);

        try (MockedStatic<OrderMapper> mockedMapper = mockStatic(OrderMapper.class)) {
            mockedMapper.when(() -> OrderMapper.mapCreateOrderRequestDtoToOrder(eq(requestDto), any(Order.class)))
                    .thenReturn(order);

            when(orderRepository.save(order)).thenReturn(order);

            String result = orderService.createOrder(requestDto);

            assertEquals("1", result);
            verify(orderRepository, times(1)).save(order);
        }
    }

    @Test
    void testFetchOrder_Found() {
        Order order = new Order();
        order.setOrderId(1L);
        OrderDto orderDto = new OrderDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        try (MockedStatic<OrderMapper> mockedMapper = mockStatic(OrderMapper.class)) {
            mockedMapper.when(() -> OrderMapper.mapOrderToOrderDto(eq(order), any(OrderDto.class)))
                    .thenReturn(orderDto);

            OrderDto result = orderService.fetchOrder("1");

            assertNotNull(result);
            assertEquals(orderDto, result);
            verify(orderRepository, times(1)).findById(1L);
        }
    }

    @Test
    void testFetchOrder_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.fetchOrder("1"));
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateOrder_Found() {
        UpdateOrderDto updateDto = new UpdateOrderDto();
        Order order = new Order();
        order.setOrderId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        try (MockedStatic<OrderMapper> mockedMapper = mockStatic(OrderMapper.class)) {
            mockedMapper.when(() -> OrderMapper.mapUpdateOrderDtoToOrder(eq(updateDto), any(Order.class)))
                    .then(invocation -> null);

            when(orderRepository.save(order)).thenReturn(order);

            String result = orderService.updateOrder(updateDto, "1");

            assertEquals("1", result);
            verify(orderRepository, times(1)).findById(1L);
            verify(orderRepository, times(1)).save(order);
        }
    }

    @Test
    void testUpdateOrder_NotFound() {
        UpdateOrderDto updateDto = new UpdateOrderDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.updateOrder(updateDto, "1"));
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteOrder_Found() {
        Order order = new Order();
        order.setOrderId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.deleteOrder("1");

        assertNotNull(order.getDeletedAt());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testDeleteOrder_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        orderService.deleteOrder("1");

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any());
    }
}
