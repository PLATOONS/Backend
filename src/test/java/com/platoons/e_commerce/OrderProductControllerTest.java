package com.platoons.e_commerce;

import com.platoons.e_commerce.controller.OrderProductController;
import com.platoons.e_commerce.dto.AddToCartRequestDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.exceptions.BadRequestException;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.service.IOrderProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class OrderProductControllerTest {

    private IOrderProductService orderProductService;
    private OrderProductController controller;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        orderProductService = mock(IOrderProductService.class);
        mockAuthentication = mock(Authentication.class);

        controller = new OrderProductController(orderProductService);
    }

    @Test
    void addToCart_ok() {
        // Arrange
        var payload = new AddToCartRequestDto("PROD-1", 2, "red");

        when(mockAuthentication.getName()).thenReturn("1");
        when(mockAuthentication.isAuthenticated()).thenReturn(true);

        doNothing().when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        // Act
        ResponseEntity<GenericResponseDto> response = controller.addToCart(payload, mockAuthentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Product added to cart", response.getBody().getMessage());
        verify(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));
    }

    @Test
    void addToCart_productNotFound_throwsException() {
        // Arrange
        var payload = new AddToCartRequestDto("PROD-NO-EXISTE", 1, null);
        when(mockAuthentication.getName()).thenReturn("1");
        when(mockAuthentication.isAuthenticated()).thenReturn(true);

        doThrow(new EntityNotFoundException("Product", "sku", "PROD-NO-EXISTE"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            controller.addToCart(payload, mockAuthentication);
        });
    }

    @Test
    void addToCart_badRequest_throwsException() {
        // Arrange
        var payload = new AddToCartRequestDto("PROD-1", 9999, "blue");
        when(mockAuthentication.getName()).thenReturn("1");
        when(mockAuthentication.isAuthenticated()).thenReturn(true);

        doThrow(new BadRequestException("Quantity is greater than available stock"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            controller.addToCart(payload, mockAuthentication);
        });
    }

    @Test
    void removeFromCart_always204() {
        // Arrange
        String productId = "PROD-1";
        when(mockAuthentication.getName()).thenReturn("1");
        when(mockAuthentication.isAuthenticated()).thenReturn(true);

        doNothing().when(orderProductService).removeFromCart(productId, "1");

        // Act
        ResponseEntity<Void> response = controller.removeFromCart(productId, mockAuthentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderProductService, times(1)).removeFromCart(eq(productId), eq("1"));
    }
}
