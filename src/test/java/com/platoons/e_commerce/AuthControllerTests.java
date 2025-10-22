package com.platoons.e_commerce;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platoons.e_commerce.controller.AuthController;
import com.platoons.e_commerce.dto.CreateUserRequestDto;
import com.platoons.e_commerce.dto.LoginRequestDto;
import com.platoons.e_commerce.service.ICustomerService;
import com.platoons.e_commerce.service.IJWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTests {

    private MockMvc mockMvc;
    private ICustomerService customerService;
    private IJWTService jwtService;
    private AuthenticationManager authenticationManager;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        customerService = mock(ICustomerService.class);
        jwtService = mock(IJWTService.class);
        authenticationManager = mock(AuthenticationManager.class);
        objectMapper = new ObjectMapper();

        AuthController authController = new AuthController(
                customerService,
                jwtService,
                authenticationManager
        );

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    // ==================== REGISTER ENDPOINT TESTS ====================

    @Test
    void testCreateCustomer_Success() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "john@example.com",
                "Password123!",
                "John",
                "Doe"
        );

        String customerId = "customer123";
        when(customerService.createCustomer(any(CreateUserRequestDto.class)))
                .thenReturn(customerId);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Successfully created customer"))
                .andExpect(header().exists("Location"))
                .andReturn();

        String locationHeader = result.getResponse().getHeader("Location");
        assertNotNull(locationHeader);
        assertTrue(locationHeader.contains("/api/v1/customer/" + customerId));

        verify(customerService, times(1)).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithMinimumValidData() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "ab",  // minimum 2 characters
                "test@test.com",
                "Pass123!",  // minimum 8 characters with all requirements
                "A",
                "B"
        );

        when(customerService.createCustomer(any(CreateUserRequestDto.class)))
                .thenReturn("customer456");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Successfully created customer"));

        verify(customerService, times(1)).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithNullUsername_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                null,
                "john@example.com",
                "Password123!",
                "John",
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithInvalidEmail_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "invalid-email",
                "Password123!",
                "John",
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithWeakPassword_NoDigit_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "john@example.com",
                "Password!",  // No digit
                "John",
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithWeakPassword_NoUppercase_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "john@example.com",
                "password123!",  // No uppercase
                "John",
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithWeakPassword_NoLowercase_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "john@example.com",
                "PASSWORD123!",  // No lowercase
                "John",
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithWeakPassword_NoSpecialChar_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "john@example.com",
                "Password123",  // No special character
                "John",
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithShortPassword_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "john@example.com",
                "Pass1!",  // Less than 8 characters
                "John",
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithShortUsername_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "a",  // Less than 2 characters
                "john@example.com",
                "Password123!",
                "John",
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithBlankFirstName_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "john@example.com",
                "Password123!",
                "",  // Blank first name
                "Doe"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithBlankLastName_ShouldFail() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(
                "johndoe",
                "john@example.com",
                "Password123!",
                "John",
                ""  // Blank last name
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    @Test
    void testCreateCustomer_WithMalformedJson_ShouldFail() throws Exception {
        String malformedJson = "{\"username\": \"johndoe\", \"email\": }";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any(CreateUserRequestDto.class));
    }

    // ==================== LOGIN ENDPOINT TESTS ====================

    @Test
    void testLogin_Success() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("johndoe", "Password123!");
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtService.generateToken(mockAuth)).thenReturn(expectedToken);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(mockAuth);
    }

    @Test
    void testLogin_WithEmail_Success() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("john@example.com", "Password123!");
        String expectedToken = "jwt.token.here";

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtService.generateToken(mockAuth)).thenReturn(expectedToken);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(mockAuth);
    }

    @Test
    void testLogin_WithIncorrectPassword_ShouldFail() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("johndoe", "WrongPassword123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Incorrect username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_WithNonExistentUser_ShouldFail() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("nonexistent", "Password123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Incorrect username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_WithNullUsername_ShouldFail() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto(null, "Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_WithNullPassword_ShouldFail() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("johndoe", null);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_WithEmptyCredentials_ShouldFail() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("", "");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Incorrect username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_WithAuthenticationReturningNull_ShouldFail() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("johndoe", "Password123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_WithUnauthenticatedResponse_ShouldFail() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("johndoe", "Password123!");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_WithMalformedJson_ShouldFail() throws Exception {
        String malformedJson = "{\"username\": \"johndoe\", \"password\": }";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }
}
