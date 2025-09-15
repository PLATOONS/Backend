package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Payment method details")
public class PaymentMethodDto {

    @Schema(
            description = "ID of the payment method",
            example = "1"
    )
    private Long methodId;

    @Schema(
            description = "Name of the payment method",
            example = "Visa, Mastercard, American Express, etc."
    )
    private String name;

    @Schema(
        description = "Description of the payment method",
        example = "Visa, Mastercard, American Express, etc."
    )
    private String description;
}
