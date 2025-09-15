package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "User creation Request")
public class CreateUserRequestDto {
    @Schema(
            description = "User's username",
            example = "joe mama"
    )
    @Size(min = 2, message = "Username must be at least 2 characters long")
    @Size(max = 32, message = "Username must be at most 32 characters long")
    @Column(unique = true)
    @NotNull
    private String username;

    @Schema(
            description = "User's email address",
            example = "user@example.com"
    )
    @Email
    @Column(unique = true)
    @NotNull
    private String email;

    @Schema(
            description = "A strong password",
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
            description = "User's first name",
            example = "Joe"
    )
    @NotNull(message = "First name is required")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(
        description = "User's last name",
        example = "Mama"
    )
    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last name is required")
    private String lastName;
}
