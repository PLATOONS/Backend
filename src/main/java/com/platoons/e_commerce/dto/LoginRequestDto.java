package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Credentials for logging in")
public class LoginRequestDto {
    @Schema(
            description = "User's username or email",
            example = "joe mama"
    )
    @NotNull
    private String username;

    @Schema(
            description = "User's password",
            example = "SuperStrongPassword$123"
    )
    @NotNull
    private String password;
}
