package com.platoons.e_commerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platoons.e_commerce.controller.OrderProductController;
import com.platoons.e_commerce.dto.AddToCartRequestDto;
import com.platoons.e_commerce.exceptions.BadRequestException;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.exceptions.GlobalExceptionHandler;
import com.platoons.e_commerce.service.IOrderProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
class OrderProductControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    IOrderProductService orderProductService;

    // 200 OK (autenticado)
    @Test
    @WithMockUser(username = "1")
    void addToCart_ok() throws Exception {
        var payload = new AddToCartRequestDto("PROD-1", 2, "red");

        mockMvc.perform(post("/api/v1/orderProduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product added to cart"));

        verify(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));
    }

    @Test
    void addToCart_unauthorized() throws Exception {
        var payload = new AddToCartRequestDto("PROD-1", 1, null);

        mockMvc.perform(post("/api/v1/orderProduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value("User not logged in"));

        verify(orderProductService, never()).addToCart(any(), any());
    }

    // 404 Not Found (producto no existe)
    @Test
    @WithMockUser(username = "1")
    void addToCart_productNotFound_404() throws Exception {
        var payload = new AddToCartRequestDto("PROD-NO-EXISTE", 1, null);

        doThrow(new EntityNotFoundException("Product not found", "PRODUCT", "PROD-NO-EXISTE"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        mockMvc.perform(post("/api/v1/orderProduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Product not found"));
    }

    @Test
    @WithMockUser(username = "1")
    void addToCart_badRequest_400() throws Exception {
        var payload = new AddToCartRequestDto("PROD-1", 9999, "blue");

        doThrow(new BadRequestException("Quantity is greater than available stock"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        mockMvc.perform(post("/api/v1/orderProduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Quantity is greater than available stock"));
    }
}
