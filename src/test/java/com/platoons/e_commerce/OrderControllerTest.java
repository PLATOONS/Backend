package com.platoons.e_commerce;

import com.platoons.e_commerce.controller.OrderController;
import com.platoons.e_commerce.dto.CreateOrderRequestDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.dto.OrderDto;
import com.platoons.e_commerce.dto.UpdateOrderDto;
import com.platoons.e_commerce.service.IOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class OrderControllerTest{

    private IOrderService orderService;
    private OrderController orderController;
    private Long orderId;

    @BeforeEach
    void setUp() {
        orderService = mock(IOrderService.class);
        orderController = new OrderController(orderService);
        orderId = 1L;
    }

    @Test
    void fetchOrder_returnsOrderDetails() {
        OrderDto dto = new OrderDto();
        dto.setOrderId(orderId);
        dto.setTotalAmount(100.0);

        when(orderService.fetchOrder(orderId.toString())).thenReturn(dto);

        ResponseEntity<OrderDto> response = orderController.fetchOrder(orderId.toString());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(100.0, response.getBody().getTotalAmount());
        verify(orderService, times(1)).fetchOrder(orderId.toString());
    }

    @Test
    void createOrder_returnsCreatedResponse() {
        CreateOrderRequestDto dto = new CreateOrderRequestDto();
        dto.setOrderId(orderId);
        dto.setCustomer("customer-1");
        dto.setSubTotalAmount(100.0);
        dto.setTotalAmout(107.0);

        when(orderService.createOrder(dto)).thenReturn(orderId.toString());

        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {

            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyString())).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/order/" + orderId));
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = orderController.createOrder(dto);

            assertEquals(201, response.getStatusCodeValue());
            assertEquals("Successfully Order", response.getBody().getMessage());
            verify(orderService, times(1)).createOrder(dto);
        }
    }

    @Test
    void updateOrder_returnsUpdatedResponse() {
        UpdateOrderDto dto = new UpdateOrderDto();
        dto.setSubTotalAmount(120.0);
        dto.setTotalAmout(130.0);

        when(orderService.updateOrder(dto, orderId.toString())).thenReturn(orderId.toString());

        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {

            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            when(builder.path(anyString())).thenReturn(builder);
            when(builder.buildAndExpand(anyString())).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(URI.create("http://localhost/api/v1/order/" + orderId));
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            ResponseEntity<GenericResponseDto> response = orderController.updateOrder(dto, orderId.toString());

            assertEquals(201, response.getStatusCodeValue());
            assertEquals("Order Updated", response.getBody().getMessage());
            verify(orderService, times(1)).updateOrder(dto, orderId.toString());
        }
    }

    @Test
    void deleteOrder_returnsNoContent() {
        doNothing().when(orderService).deleteOrder(orderId.toString());

        ResponseEntity<Object> response = orderController.deleteOrder(orderId.toString());

        assertEquals(204, response.getStatusCodeValue());
        verify(orderService, times(1)).deleteOrder(orderId.toString());
    }
}