package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Customer details")
public class CustomerDto {
    @Schema(
            description = "The customer's ID",
            example = "b05ac192-6054-4696-b741-e471e6e472ec"
    )
    private String customerId;

    @Schema(
            description = "Customer's registration date",
            example = "2025-09-14T13:19:13"
    )
    private LocalDateTime registrationDate;

    @Schema(
            description = "Customer's username",
            example = "joe mama"
    )
    private String username;

    @Schema(
            description = "Customer's email address",
            example = "user@example.com"
    )
    private String email;

    @Schema(
            description = "Customer's phone number",
            example = "+502-98765432"
    )
    private String phoneNumber;

    @Schema(
            description = "Customer's first name",
            example = "Joe"
    )
    private String firstName;

    @Schema(
        description = "Customer's last name",
        example = "Mama"
    )
    private String lastName;
}
