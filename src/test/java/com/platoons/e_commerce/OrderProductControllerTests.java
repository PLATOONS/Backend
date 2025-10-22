package com.platoons.e_commerce;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platoons.e_commerce.controller.OrderProductController;
import com.platoons.e_commerce.dto.AddToCartRequestDto;
import com.platoons.e_commerce.dto.UpdateQuantityRequestDto;
import com.platoons.e_commerce.exceptions.BadRequestException;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.service.IOrderProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

public class OrderProductControllerTests {

    private MockMvc mockMvc;
    private IOrderProductService orderProductService;
    private ObjectMapper objectMapper;
    private Authentication mockAuth;

    @BeforeEach
    void setUp() {
        orderProductService = mock(IOrderProductService.class);
        objectMapper = new ObjectMapper();
        mockAuth = mock(Authentication.class);

        OrderProductController controller = new OrderProductController(orderProductService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<String> handleBadRequest(BadRequestException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // ==================== ADD TO CART ENDPOINT TESTS ====================

    @Test
    void testAddToCart_Success() throws Exception {
        AddToCartRequestDto request = new AddToCartRequestDto("PROD-123", 2, "red");
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doNothing().when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("testuser"));

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Product added to cart"));

        verify(orderProductService, times(1)).addToCart(any(AddToCartRequestDto.class), eq("testuser"));
    }

    @Test
    void testAddToCart_WithoutColor_Success() throws Exception {
        AddToCartRequestDto request = new AddToCartRequestDto("PROD-456", 1, null);
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("user123");
        
        doNothing().when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("user123"));

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product added to cart"));

        verify(orderProductService, times(1)).addToCart(any(AddToCartRequestDto.class), eq("user123"));
    }

    @Test
    void testAddToCart_WithLargeQuantity_Success() throws Exception {
        AddToCartRequestDto request = new AddToCartRequestDto("PROD-789", 100, "blue");
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doNothing().when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("testuser"));

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product added to cart"));

        verify(orderProductService, times(1)).addToCart(any(AddToCartRequestDto.class), eq("testuser"));
    }

    @Test
    void testAddToCart_NotAuthenticated_ShouldFail() throws Exception {
        AddToCartRequestDto request = new AddToCartRequestDto("PROD-123", 2, "red");
        
        when(mockAuth.isAuthenticated()).thenReturn(false);

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isUnauthorized());

        verify(orderProductService, never()).addToCart(any(AddToCartRequestDto.class), anyString());
    }

    @Test
    void testAddToCart_NullAuthentication_ShouldFail() throws Exception {
        AddToCartRequestDto request = new AddToCartRequestDto("PROD-123", 2, "red");

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(orderProductService, never()).addToCart(any(AddToCartRequestDto.class), anyString());
    }

    @Test
    void testAddToCart_WithBlankProductId_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":\"\",\"quantity\":2,\"color\":\"red\"}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).addToCart(any(AddToCartRequestDto.class), anyString());
    }

    @Test
    void testAddToCart_WithNullProductId_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":null,\"quantity\":2,\"color\":\"red\"}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).addToCart(any(AddToCartRequestDto.class), anyString());
    }

    @Test
    void testAddToCart_WithNullQuantity_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":\"PROD-123\",\"quantity\":null,\"color\":\"red\"}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).addToCart(any(AddToCartRequestDto.class), anyString());
    }

    @Test
    void testAddToCart_WithZeroQuantity_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":\"PROD-123\",\"quantity\":0,\"color\":\"red\"}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).addToCart(any(AddToCartRequestDto.class), anyString());
    }

    @Test
    void testAddToCart_WithNegativeQuantity_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":\"PROD-123\",\"quantity\":-5,\"color\":\"red\"}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).addToCart(any(AddToCartRequestDto.class), anyString());
    }

    @Test
    void testAddToCart_ProductNotFound_ShouldFail() throws Exception {
        AddToCartRequestDto request = new AddToCartRequestDto("NONEXISTENT", 2, "red");
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doThrow(new EntityNotFoundException("Product", "productId", "NONEXISTENT"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("testuser"));

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isNotFound());

        verify(orderProductService, times(1)).addToCart(any(AddToCartRequestDto.class), eq("testuser"));
    }

    @Test
    void testAddToCart_QuantityExceedsStock_ShouldFail() throws Exception {
        AddToCartRequestDto request = new AddToCartRequestDto("PROD-123", 100, "red");
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doThrow(new BadRequestException("Quantity is greater than available stock"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("testuser"));

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, times(1)).addToCart(any(AddToCartRequestDto.class), eq("testuser"));
    }

    @Test
    void testAddToCart_WithMalformedJson_ShouldFail() throws Exception {
        String malformedJson = "{\"productId\":\"PROD-123\",\"quantity\":}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(post("/api/v1/orderProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).addToCart(any(AddToCartRequestDto.class), anyString());
    }

    // ==================== REMOVE FROM CART ENDPOINT TESTS ====================

    @Test
    void testRemoveFromCart_Success() throws Exception {
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doNothing().when(orderProductService).removeFromCart(eq("PROD-123"), eq("testuser"));

        mockMvc.perform(delete("/api/v1/orderProduct/PROD-123")
                        .principal(mockAuth))
                .andExpect(status().isNoContent());

        verify(orderProductService, times(1)).removeFromCart(eq("PROD-123"), eq("testuser"));
    }

    @Test
    void testRemoveFromCart_ProductNotInCart_Success() throws Exception {
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        // Service doesn't throw exception even if product not found (soft delete behavior)
        doNothing().when(orderProductService).removeFromCart(eq("NONEXISTENT"), eq("testuser"));

        mockMvc.perform(delete("/api/v1/orderProduct/NONEXISTENT")
                        .principal(mockAuth))
                .andExpect(status().isNoContent());

        verify(orderProductService, times(1)).removeFromCart(eq("NONEXISTENT"), eq("testuser"));
    }

    @Test
    void testRemoveFromCart_NotAuthenticated_ShouldFail() throws Exception {
        when(mockAuth.isAuthenticated()).thenReturn(false);

        mockMvc.perform(delete("/api/v1/orderProduct/PROD-123")
                        .principal(mockAuth))
                .andExpect(status().isUnauthorized());

        verify(orderProductService, never()).removeFromCart(anyString(), anyString());
    }

    @Test
    void testRemoveFromCart_NullAuthentication_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/v1/orderProduct/PROD-123"))
                .andExpect(status().isUnauthorized());

        verify(orderProductService, never()).removeFromCart(anyString(), anyString());
    }

    @Test
    void testRemoveFromCart_WithSpecialCharactersInProductId_Success() throws Exception {
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doNothing().when(orderProductService).removeFromCart(eq("PROD-123-ABC"), eq("testuser"));

        mockMvc.perform(delete("/api/v1/orderProduct/PROD-123-ABC")
                        .principal(mockAuth))
                .andExpect(status().isNoContent());

        verify(orderProductService, times(1)).removeFromCart(eq("PROD-123-ABC"), eq("testuser"));
    }

    // ==================== UPDATE QUANTITY ENDPOINT TESTS ====================

    @Test
    void testUpdateQuantity_Success() throws Exception {
        UpdateQuantityRequestDto request = new UpdateQuantityRequestDto();
        request.setProductId("PROD-123");
        request.setQuantity(5);
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doNothing().when(orderProductService).updateQuantity(eq("PROD-123"), eq(5), eq("testuser"));

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Quantity for product PROD-123 updated to 5"));

        verify(orderProductService, times(1)).updateQuantity(eq("PROD-123"), eq(5), eq("testuser"));
    }

    @Test
    void testUpdateQuantity_ToMinimumQuantity_Success() throws Exception {
        UpdateQuantityRequestDto request = new UpdateQuantityRequestDto();
        request.setProductId("PROD-456");
        request.setQuantity(1);
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doNothing().when(orderProductService).updateQuantity(eq("PROD-456"), eq(1), eq("testuser"));

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Quantity for product PROD-456 updated to 1"));

        verify(orderProductService, times(1)).updateQuantity(eq("PROD-456"), eq(1), eq("testuser"));
    }

    @Test
    void testUpdateQuantity_ToLargeQuantity_Success() throws Exception {
        UpdateQuantityRequestDto request = new UpdateQuantityRequestDto();
        request.setProductId("PROD-789");
        request.setQuantity(999);
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doNothing().when(orderProductService).updateQuantity(eq("PROD-789"), eq(999), eq("testuser"));

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Quantity for product PROD-789 updated to 999"));

        verify(orderProductService, times(1)).updateQuantity(eq("PROD-789"), eq(999), eq("testuser"));
    }

    @Test
    void testUpdateQuantity_NotAuthenticated_ShouldFail() throws Exception {
        UpdateQuantityRequestDto request = new UpdateQuantityRequestDto();
        request.setProductId("PROD-123");
        request.setQuantity(5);
        
        when(mockAuth.isAuthenticated()).thenReturn(false);

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isUnauthorized());

        verify(orderProductService, never()).updateQuantity(anyString(), anyInt(), anyString());
    }

    @Test
    void testUpdateQuantity_NullAuthentication_ShouldFail() throws Exception {
        UpdateQuantityRequestDto request = new UpdateQuantityRequestDto();
        request.setProductId("PROD-123");
        request.setQuantity(5);

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(orderProductService, never()).updateQuantity(anyString(), anyInt(), anyString());
    }

    @Test
    void testUpdateQuantity_WithBlankProductId_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":\"\",\"quantity\":5}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).updateQuantity(anyString(), anyInt(), anyString());
    }

    @Test
    void testUpdateQuantity_WithNullProductId_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":null,\"quantity\":5}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).updateQuantity(anyString(), anyInt(), anyString());
    }

    @Test
    void testUpdateQuantity_WithZeroQuantity_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":\"PROD-123\",\"quantity\":0}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).updateQuantity(anyString(), anyInt(), anyString());
    }

    @Test
    void testUpdateQuantity_WithNegativeQuantity_ShouldFail() throws Exception {
        String requestJson = "{\"productId\":\"PROD-123\",\"quantity\":-10}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).updateQuantity(anyString(), anyInt(), anyString());
    }

    @Test
    void testUpdateQuantity_ProductNotFound_ShouldFail() throws Exception {
        UpdateQuantityRequestDto request = new UpdateQuantityRequestDto();
        request.setProductId("NONEXISTENT");
        request.setQuantity(5);
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doThrow(new EntityNotFoundException("Product", "productId", "NONEXISTENT"))
                .when(orderProductService).updateQuantity(eq("NONEXISTENT"), eq(5), eq("testuser"));

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isNotFound());

        verify(orderProductService, times(1)).updateQuantity(eq("NONEXISTENT"), eq(5), eq("testuser"));
    }

    @Test
    void testUpdateQuantity_QuantityExceedsStock_ShouldFail() throws Exception {
        UpdateQuantityRequestDto request = new UpdateQuantityRequestDto();
        request.setProductId("PROD-123");
        request.setQuantity(1000);
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doThrow(new BadRequestException("Quantity is greater than available stock"))
                .when(orderProductService).updateQuantity(eq("PROD-123"), eq(1000), eq("testuser"));

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, times(1)).updateQuantity(eq("PROD-123"), eq(1000), eq("testuser"));
    }

    @Test
    void testUpdateQuantity_NoActiveCart_ShouldFail() throws Exception {
        UpdateQuantityRequestDto request = new UpdateQuantityRequestDto();
        request.setProductId("PROD-123");
        request.setQuantity(5);
        
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");
        
        doThrow(new BadRequestException("No active cart found for user"))
                .when(orderProductService).updateQuantity(eq("PROD-123"), eq(5), eq("testuser"));

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, times(1)).updateQuantity(eq("PROD-123"), eq(5), eq("testuser"));
    }

    @Test
    void testUpdateQuantity_WithMalformedJson_ShouldFail() throws Exception {
        String malformedJson = "{\"productId\":\"PROD-123\",\"quantity\":}";

        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(mockAuth.getName()).thenReturn("testuser");

        mockMvc.perform(patch("/api/v1/orderProduct/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson)
                        .principal(mockAuth))
                .andExpect(status().isBadRequest());

        verify(orderProductService, never()).updateQuantity(anyString(), anyInt(), anyString());
    }
}
