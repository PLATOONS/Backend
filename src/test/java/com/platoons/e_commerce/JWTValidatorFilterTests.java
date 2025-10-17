package com.platoons.e_commerce;

import com.platoons.e_commerce.filter.JWTValidatorFilter;
import com.platoons.e_commerce.service.impl.JWTServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class JWTValidatorFilterTests {

    @Test
    void validJWTGiven() throws ServletException, IOException {
        // Arrange
        String validJwt = "Bearer Valid JWT";

        HttpServletRequest request = mock(HttpServletRequest.class);

        HttpServletResponse response = mock(HttpServletResponse.class);

        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization"))
                .thenReturn(validJwt);

        JWTServiceImpl jwtService = mock(JWTServiceImpl.class);

        doNothing().when(jwtService).validateToken(validJwt);

        JWTValidatorFilter filter = new JWTValidatorFilter(jwtService);

        doNothing().when(filterChain).doFilter(any(), any());

        // Assert
        Assertions.assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void shouldNotFilterTest(){
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String path = "/api/v1/auth/login";
        when(request.getServletPath()).thenReturn(path);

        JWTServiceImpl jwtService = mock(JWTServiceImpl.class);

        JWTValidatorFilter filter = new JWTValidatorFilter(jwtService);
        // Act
        try {
            boolean ans = filter.shouldNotFilter(request);

            // Assert
            Assertions.assertTrue(ans);
        }
        catch (Exception exception){
            Assertions.fail();
        }
    }
}
