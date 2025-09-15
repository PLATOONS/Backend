package com.platoons.e_commerce.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Update payment request")
public class UpdatePaymentDto {

    @Schema(
            description = "ID of the payment",
            example = "1"
    )
    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @Schema(
            description = "Amount of the payment",
            example = "100.00"
    )
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount;

    @Schema(
            description = "Payment date",
            example = "2025-09-14T13:30:46"
    )
    @NotNull(message = "Payment date is required")
    private LocalDateTime paymentDate;

    @Schema(
            description = "Name of the bill",
            example = "Bill for order 1"
    )
    @NotBlank(message = "Bill name is required")
    @Size(min = 3, max = 100, message = "Bill name must be between 3 and 100 characters")
    private String billName;

    @Schema(
            description = "Description of the payment",
            example = "Payment for order 1"
    )
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Schema(
            description = "ID of the payment method",
            example = "1"
    )
    @NotNull(message = "Payment method is required")
    private Long methodId;

    @Schema(
        description = "ID of the payment status",
        example = "1"
    )
    @NotNull(message = "Payment status is required")
    private Long statusId;
}
