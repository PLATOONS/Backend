package com.platoons.e_commerce.controller;

import java.util.List;
import java.net.URI;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.platoons.e_commerce.dto.*;
import com.platoons.e_commerce.service.IOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
@Tag(name = "Order Controller", description = "APIs for managing orders")
public class OrderController {

    private final IOrderService orderService;

    @Operation(summary = "Get order by ID", description = "Retrieves order details by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order",
                     content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponseDto> fetchOrder(
            @Parameter(description = "ID of the order to be retrieved", required = true)
            @PathVariable String orderId) {
        log.info("Fetching order with id {}", orderId);
        return ResponseEntity.ok(orderService.fetchOrder(orderId));
    }

    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully",
                     content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponseDto> createOrder(
            @Parameter(description = "Order details to be created", required = true)
            @Valid @RequestBody CreateOrderRequestDto orderDto) {
        log.info("Creating order");
        String orderId = orderService.createOrder(orderDto);

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/order/{id}")
                .buildAndExpand(orderId)
                .toUri();

        log.info("Order created with id {}", orderId);
        return ResponseEntity.created(uri).body(new GenericResponseDto("Successfully created order"));
    }

    @Operation(summary = "Delete an order", description = "Removes an order and its associated data via soft delete")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Order deleted successfully")
    })
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID of the order to be deleted", required = true)
            @PathVariable String orderId) {
        log.info("Deleting order with id {}", orderId);
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update order information", description = "Updates the details of an existing order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order updated successfully",
                     content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping(value = "/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponseDto> updateOrder(
            @Parameter(description = "Updated order details", required = true)
            @Valid @RequestBody UpdateOrderDto orderDto,
            @Parameter(description = "ID of the order to be updated", required = true)
            @PathVariable String orderId) {
        log.info("Updating order with id {}", orderId);

        String savedOrderId = orderService.updateOrder(orderDto, orderId);

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/order/{id}")
                .buildAndExpand(savedOrderId)
                .toUri();

        log.info("Order updated with id {}", savedOrderId);
        return ResponseEntity.created(uri).body(new GenericResponseDto("Order updated successfully"));
    }

    @Operation(summary = "Get all orders for the logged in user", description = "Returns all orders of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of user's orders",
                     content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "User not logged in")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User not logged in");
        }

        String username = authentication.getName();
        List<OrderResponseDto> orders = orderService.getOrdersByUser(username);
        return ResponseEntity.ok(orders);
    }
}
