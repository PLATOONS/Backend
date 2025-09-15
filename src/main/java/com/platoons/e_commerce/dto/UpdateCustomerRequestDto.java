package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Update customer request")
public class UpdateCustomerRequestDto {
    @Schema(
            description = "Updated username",
            example = "xXJoeMamaXx"
    )
    @Size(min = 2, message = "Username must be at least 2 characters long")
    @Size(max = 32, message = "Username must be at most 32 characters long")
    @NotNull
    private String username;

    @Schema(
            description = "A strong updated password",
            example = "SuperStrongPassword$123"
    )
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?.*_-]).{8,64}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    private String password;

    @Schema(
            description = "Updated email address",
            example = "joe.mama@gmail.com"
    )
    @Email
    @NotNull
    private String email;

    @Schema(
            description = "Updated phone number",
            example = "+502-98765432"
    )
    @Pattern(regexp = "^\\+([0-9]{1,4})[-\\s]?([0-9]{1,15})$", message = "Must be a valid phone number")
    private String phoneNumber;

    @Schema(
            description = "Updated first name",
            example = "Joe"
    )
    @NotNull(message = "First name is required")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(
        description = "Updated last name",
        example = "Mama"
    )
    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last name is required")
    private String lastName;
}
