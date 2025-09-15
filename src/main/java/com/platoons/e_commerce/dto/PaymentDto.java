package com.platoons.e_commerce.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Payment details")
public class PaymentDto {

    @Schema(
            description = "ID of the payment",
            example = "1"
    )
    private Long paymentId;

    @Schema(
            description = "Amount of the payment",
            example = "100.0"
    )
    private Double amount;

    @Schema(
            description = "Date when the payment was done",
            example = "2025-09-14T13:19:13"
    )
    private LocalDateTime paymentDate;

    @Schema(
            description = "Name of the bill",
            example = "Bill for order 123"
    )
    private String billName;

    @Schema(
            description = "Description of the payment",
            example = "Payment for order 123"
    )
    private String description;

    @Schema(
        description = "Payment method details"
    )
    private PaymentMethodDto paymentMethod;

    @Schema(
        description = "Payment status details"
    )
    private PaymentStatusDto paymentStatus;
}
