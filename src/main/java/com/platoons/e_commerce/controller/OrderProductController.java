package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.platoons.e_commerce.service.IOrderProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/orderProduct")
@Tag(name = "OrderProduct Controller", description = "APIs for cart operations")
public class OrderProductController {

    private final IOrderProductService orderProductService;

    @Operation(summary = "Add product to cart", description = "Creates or reuses the user's order with status CART and adds (or increments) the line.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product added to cart", content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input / stock"),
            @ApiResponse(responseCode = "401", description = "User not logged in"),
            @ApiResponse(responseCode = "404", description = "Product / Customer / Status not found")
    })
    @PostMapping
    public ResponseEntity<GenericResponseDto> addToCart(@Valid @RequestBody AddToCartRequestDto request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User not logged in");
        }
        String username = authentication.getName();
        orderProductService.addToCart(request, username);
        return ResponseEntity.ok(new GenericResponseDto("Product added to cart"));
    }

    @Operation(summary = "Remove product from cart (soft delete)", description = "Soft deletes an orderProduct line (sets deletedAt) from the user's CART. Always returns 204.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content (even if the product is not in the cart)"),
            @ApiResponse(responseCode = "401", description = "User not logged in")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable String productId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User not logged in");
        }
        String username = authentication.getName();
        log.info("RemoveFromCart user={}, productId={}", username, productId);
        orderProductService.removeFromCart(productId, username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update product quantity in cart", description = "Updates the quantity of a product in the user's order. Checks for available stock.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantity updated", content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Quantity exceeds available stock"),
            @ApiResponse(responseCode = "401", description = "User not logged in"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PatchMapping("/quantity")
    public ResponseEntity<GenericResponseDto> updateQuantity(
            @Valid @RequestBody UpdateQuantityRequestDto request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User not logged in");
        }
        String username = authentication.getName();
        orderProductService.updateQuantity(request.getProductId(), request.getQuantity(), username);
        return ResponseEntity.ok(new GenericResponseDto(
                "Quantity for product " + request.getProductId() + " updated to " + request.getQuantity()));
    }

    @Operation(summary = "Fetch products in cart", description = "Fetches the products in the user's cart.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products fetched successfully", content = @Content(schema = @Schema(implementation = FetchProductResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "User not logged in")
    })
    @GetMapping
    public ResponseEntity<List<CartProductsDto>> fetchCartProducts(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(orderProductService.fetchCartProducts(username));
    }
}
