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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(OrderProductController.class)
@AutoConfigureMockMvc // <- CAMBIO APLICADO AQUÍ
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
class OrderProductControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    IOrderProductService orderProductService;

    // 200 OK
    @Test
    void addToCart_ok() throws Exception {
        var payload = new AddToCartRequestDto("PROD-1", 2, "red");

        mockMvc.perform(post("/api/v1/orderProduct")
                .with(user("1"))
                .with(csrf())
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
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());

        verify(orderProductService, never()).addToCart(any(), any());
    }

    // 404 Not Found
    @Test
    void addToCart_productNotFound_404() throws Exception {
        var payload = new AddToCartRequestDto("PROD-NO-EXISTE", 1, null);

        doThrow(new EntityNotFoundException("Product", "sku", "PROD-NO-EXISTE"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        mockMvc.perform(post("/api/v1/orderProduct")
                .with(user("1"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Could not find Product by sku with the value: PROD-NO-EXISTE"));
    }

    // 400 Bad Request
    @Test
    void addToCart_badRequest_400() throws Exception {
        var payload = new AddToCartRequestDto("PROD-1", 9999, "blue");

        doThrow(new BadRequestException("Quantity is greater than available stock"))
                .when(orderProductService).addToCart(any(AddToCartRequestDto.class), eq("1"));

        mockMvc.perform(post("/api/v1/orderProduct")
                .with(user("1"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Quantity is greater than available stock"));
    }

    // 204 cuando existe o no existe
    @Test
    @WithMockUser(username = "1")
    void removeFromCart_always204() throws Exception {
        mockMvc.perform(delete("/api/v1/orderProduct/PROD-1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(orderProductService, times(1)).removeFromCart(eq("PROD-1"), eq("1"));
    }

    // 401 cuando no hay autenticación
    @Test
    void removeFromCart_unauthorized_401() throws Exception {
        mockMvc.perform(delete("/api/v1/orderProduct/PROD-1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(orderProductService, never()).removeFromCart(any(), any());
    }

    @Test
    @WithMockUser(username = "2")
    void removeFromCart_notFoundStill204() throws Exception {
        mockMvc.perform(delete("/api/v1/orderProduct/PROD-NO-EXISTE")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(orderProductService, times(1)).removeFromCart(eq("PROD-NO-EXISTE"), eq("2"));
    }

}
