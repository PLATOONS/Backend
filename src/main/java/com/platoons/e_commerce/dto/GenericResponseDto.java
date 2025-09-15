package com.platoons.e_commerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GenericResponseDto {
    @Schema(
        description = "Response message",
        example = "Operation completed successfully"
    )
    private String message;
}
