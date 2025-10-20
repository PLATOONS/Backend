package com.platoons.e_commerce;

import com.platoons.e_commerce.controller.OrderProductController;
import com.platoons.e_commerce.dto.AddToCartRequestDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.exceptions.BadRequestException;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.service.IOrderProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProductControllerTest {

    @Mock
    private IOrderProductService orderProductService;

    @InjectMocks
    private OrderProductController controller;

    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        mockAuthentication = mock(Authentication.class);
    }

    // 200 OK
    @Test
    void addToCart_ok() {
        var payload = new AddToCartRequestDto("PROD-1", 2, "red");
        when(mockAuthentication.getName()).thenReturn("1");

        doNothing().when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        ResponseEntity<GenericResponseDto> response = controller.addToCart(payload, mockAuthentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // 👇 CAMBIO AQUÍ: Usamos .getMessage() porque es una clase con un getter
        assertEquals("Product added to cart", response.getBody().getMessage());

        verify(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));
    }

    // 404 Not Found
    @Test
    void addToCart_productNotFound_throwsException() {
        var payload = new AddToCartRequestDto("PROD-NO-EXISTE", 1, null);
        when(mockAuthentication.getName()).thenReturn("1");

        doThrow(new EntityNotFoundException("Product", "sku", "PROD-NO-EXISTE"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        assertThrows(EntityNotFoundException.class, () -> {
            controller.addToCart(payload, mockAuthentication);
        });
    }

    // 400 Bad Request
    @Test
    void addToCart_badRequest_throwsException() {
        var payload = new AddToCartRequestDto("PROD-1", 9999, "blue");
        when(mockAuthentication.getName()).thenReturn("1");

        doThrow(new BadRequestException("Quantity is greater than available stock"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        assertThrows(BadRequestException.class, () -> {
            controller.addToCart(payload, mockAuthentication);
        });
    }

    // 204 No Content
    @Test
    void removeFromCart_always204() {
        when(mockAuthentication.getName()).thenReturn("1");
        String productId = "PROD-1";

        doNothing().when(orderProductService).removeFromCart(productId, "1");

        ResponseEntity<Void> response = controller.removeFromCart(productId, mockAuthentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderProductService, times(1)).removeFromCart(eq(productId), eq("1"));
    }
}
