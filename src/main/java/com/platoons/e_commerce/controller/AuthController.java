package com.platoons.e_commerce.controller;

import com.platoons.e_commerce.dto.CreateUserRequestDto;
import com.platoons.e_commerce.dto.GenericResponseDto;
import com.platoons.e_commerce.dto.LoginRequestDto;
import com.platoons.e_commerce.service.ICustomerService;
import com.platoons.e_commerce.service.IJWTService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "APIs for handling authentication")
public class AuthController {

    private final ICustomerService customerService;
    private final IJWTService jwtService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Create a new customer", description = "Registers a new customer with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/register")
    public ResponseEntity<GenericResponseDto> createCustomer(
            @Parameter(description = "Customer details to be created", required = true)
            @Valid @RequestBody CreateUserRequestDto customerDto) {
        log.info("Creating customer");

        String customerId = customerService.createCustomer(customerDto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/customer/{id}")
                .build(String.valueOf(customerId));

        log.info("Customer created with id {}", customerId);

        return ResponseEntity.created(uri).body(
                new GenericResponseDto("Successfully created customer"));
    }

    @Operation(
            summary = "Authenticate a user",
            description = "Authenticates a user with the provided credentials and returns a JWT token if successful",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful",
                            content = @Content(
                                    mediaType = "text/plain",
                                    schema = @Schema(description = "JWT token for authenticated user")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content
                    )
            }
    )
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE,
            value = "/login"
    )
    public ResponseEntity<String> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequestDto credentials) {

        log.info("Logging in user {}", credentials.getUsername());

        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(credentials.getUsername(), credentials.getPassword());
        Authentication authenticationResponse = authenticationManager.authenticate(authentication);
        if(authenticationResponse == null || !authenticationResponse.isAuthenticated())
            throw new BadCredentialsException("Incorrect username or password");

        String jwt = jwtService.generateToken(authenticationResponse);

        log.info("Successfully logged in");

        return ResponseEntity.ok().body(jwt);
    }
}
