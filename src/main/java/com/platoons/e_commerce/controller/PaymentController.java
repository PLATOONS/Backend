package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.CreatePaymentRequestDto;
import com.platoons.e_commerce.dto.ErrorResponseDto;
import com.platoons.e_commerce.dto.PaymentDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.dto.UpdatePaymentDto;
import com.platoons.e_commerce.service.IPaymentService;
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
 * Controller for managing payment operations.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment Controller", description = "APIs for managing payments")
public class PaymentController {
    private final IPaymentService paymentService;

    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved payment",
                     content = @Content(schema = @Schema(implementation = PaymentDto.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping(value = "/{paymentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentDto> fetchPayment(
            @Parameter(description = "ID of the payment to be retrieved", required = true)
            @PathVariable Long paymentId) {
        log.info("Fetching payment");

        return ResponseEntity.ok(paymentService.fetchPayment(paymentId));
    }

    @Operation(summary = "Create a new payment", description = "Processes a new payment with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment processed successfully",
                     content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payment details",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponseDto> createPayment(
            @Parameter(description = "Payment details to be processed", required = true)
            @Valid @RequestBody CreatePaymentRequestDto paymentDto) {
        log.info("Creating Payment");
        String paymentId = paymentService.createPayment(paymentDto);

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/payment/{id}")
                .build(String.valueOf(paymentId));

        log.info("Payment created with id {}", paymentId);

        return ResponseEntity.created(uri).body(new GenericResponseDto("Successful Payment"));
    }

    @Operation(summary = "Delete a payment record", description = "Removes a payment record from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Payment record deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Object> deletePayment(
            @Parameter(description = "ID of the payment to be deleted", required = true)
            @PathVariable Long paymentId) {
        log.info("Deleting Payment");

        paymentService.deletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update payment information", description = "Updates the details of an existing payment record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment updated successfully",
                     content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping(value = "/{paymentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponseDto> updatePayment(
            @Parameter(description = "Updated payment details", required = true)
            @Valid @RequestBody UpdatePaymentDto paymentDto,
            @Parameter(description = "ID of the payment to be updated", required = true)
            @PathVariable Long paymentId) {
        log.info("Updating Payment");

        String savedPaymentId = paymentService.updatePayment(paymentDto, paymentId);

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/payment/{id}")
                .build(String.valueOf(savedPaymentId));

        log.info("Payment updated with id {}", savedPaymentId);

        return ResponseEntity.created(uri).body(new GenericResponseDto(" Payment updated"));
    }

}
