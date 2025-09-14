package com.platoons.e_commerce.exceptions;

import com.platoons.e_commerce.dto.ErrorResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        webRequest = new ServletWebRequest(new MockHttpServletRequest());
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnBadRequest() {
        // Arrange
        FieldError fieldError1 = new FieldError("orderDto", "totalAmount", "must be greater than 0");
        FieldError fieldError2 = new FieldError("orderDto", "userId", "must not be null");
        List<ObjectError> errors = Arrays.asList(fieldError1, fieldError2);
        
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(errors);
        
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Object> response = globalExceptionHandler
                .handleMethodArgumentNotValid(ex, null, HttpStatus.BAD_REQUEST, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(2, responseBody.size());
        assertEquals("must be greater than 0", responseBody.get("totalAmount"));
        assertEquals("must not be null", responseBody.get("userId"));
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerError() {
        // Arrange
        Exception exception = new Exception("Unexpected error occurred");

        // Act
        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler
                .handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Unexpected error occurred", responseBody.getErrorMessage());
        assertNotNull(responseBody.getErrorTime());
    }

    @Test
    void handleBadRequestException_ShouldReturnBadRequest() {
        // Arrange
        String errorMessage = "Invalid request parameters";
        BadRequestException exception = new BadRequestException(errorMessage);

        // Act
        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler
                .handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(errorMessage, responseBody.getErrorMessage());
        assertNotNull(responseBody.getErrorTime());
    }

    @Test
    void handleEntityNotFoundException_ShouldReturnNotFound() {
        // Arrange
        String entity = "Entity";
        String property = "Property";
        String value = "Value";
        EntityNotFoundException exception = new EntityNotFoundException(entity, property, value);

        // Act
        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler
                .handleEntityNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getErrorTime());

        String errorMessage = responseBody.getErrorMessage();
        assertTrue(errorMessage.contains(entity));
        assertTrue(errorMessage.contains(property));
        assertTrue(errorMessage.contains(value));
    }
}
