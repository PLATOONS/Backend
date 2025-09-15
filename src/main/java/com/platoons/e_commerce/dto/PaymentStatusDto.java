package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Payment status details")
public class PaymentStatusDto {

    @Schema(
            description = "ID of the payment status",
            example = "1"
    )
    private Long statusId;

    @Schema(
            description = "Name of the payment status",
            example = "Paid"
    )
    private String name;

    @Schema(
        description = "Description of the payment status",
        example = "Payment has been successfully processed and completed"
    )
    private String description;
}
