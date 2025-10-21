package com.platoons.e_commerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platoons.e_commerce.dto.AddToCartRequestDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.dto.UpdateQuantityRequestDto;
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
        String userId = authentication.getName();
        orderProductService.addToCart(request, userId);
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
        String userId = authentication.getName();
        log.info("RemoveFromCart user={}, productId={}", userId, productId);
        orderProductService.removeFromCart(productId, userId);
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
        String userId = authentication.getName();
        orderProductService.updateQuantity(request.getProductId(), request.getQuantity(), userId);
        return ResponseEntity.ok(new GenericResponseDto(
                "Quantity for product " + request.getProductId() + " updated to " + request.getQuantity()));
    }
}
