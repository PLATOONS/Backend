package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.*;
import com.platoons.e_commerce.service.ICustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller for managing customer operations.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer")
@Tag(name = "Customer Controller", description = "APIs for managing customer information")
public class CustomerController {

        private final ICustomerService customerService;

        @Operation(summary = "Get customer by ID", description = "Retrieves customer details by their unique identifier")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customer", 
                         content = @Content(schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                         content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        @GetMapping(value = "/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<CustomerDto> fetchCustomer(
                @Parameter(description = "ID of the customer to be retrieved", required = true)
                @PathVariable String customerId) {
                log.info("Fetching customer");

                return ResponseEntity.ok(customerService.fetchCustomer(customerId));
        }

        @Operation(summary = "Create a new customer", description = "Registers a new customer with the provided details")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully", 
                         content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
        })
        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<GenericResponseDto> createCustomer(
                @Parameter(description = "Customer details to be created", required = true)
                @Valid @RequestBody CreateUserRequestDto customerDto) {
                log.info("Creating customer");

                String customerId = customerService.createCustomer(customerDto);

                URI uri = ServletUriComponentsBuilder
                                .fromCurrentContextPath()
                                .path("/api/v1/customer/{id}")
                                .build(String.valueOf(customerId));

                log.info("Customer created with id {}", customerId);

                return ResponseEntity.created(uri).body(
                                new GenericResponseDto("Successfully created customer"));
        }

        @Operation(summary = "Delete a customer", description = "Removes a customer and their associated data via soft delete")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
        })
        @DeleteMapping("/{customerId}")
        public ResponseEntity<Object> deleteCustomer(
                @Parameter(description = "ID of the customer to be deleted", required = true)
                @PathVariable String customerId) {
                log.info("Deleting customer");

                customerService.deleteCustomer(customerId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Update customer information", description = "Updates the details of an existing customer")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer updated successfully", 
                         content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                         content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        @PutMapping(value = "/{customerId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<GenericResponseDto> updateCustomer(
                @Parameter(description = "Updated customer details", required = true)
                @Valid @RequestBody UpdateCustomerRequestDto customerDto,
                @Parameter(description = "ID of the customer to be updated", required = true)
                @PathVariable String customerId) {
                log.info("Updating customer");

                String savedCustomerId = customerService.updateCustomer(customerDto, customerId);

                URI uri = ServletUriComponentsBuilder
                                .fromCurrentContextPath()
                                .path("/api/v1/customer/{id}")
                                .build(String.valueOf(savedCustomerId));

                log.info("Customer updated with id {}", savedCustomerId);

                return ResponseEntity.created(uri).body(
                                new GenericResponseDto("Successfully updated customer"));
        }
}
