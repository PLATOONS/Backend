package com.platoons.e_commerce;

import com.platoons.e_commerce.service.IJWTService;
import com.platoons.e_commerce.service.impl.JWTServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JWTServiceImplTests {

    @Mock
    private Environment env;
    
    private IJWTService jwtService;
    private final String secret = "testSecretKeyForJWTTesting1234567890";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        jwtService = new JWTServiceImpl(env);
    }

    @Test
    void generateToken_WithValidAuthentication_ReturnsToken() {
        // Arrange
        when(env.getProperty("JWT_SECRET", "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4"))
                .thenReturn(secret);

        String username = "testuser";
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            username, null, authorities);

        // Act
        String token = jwtService.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT has 3 parts
    }

    @Test
    void generateToken_WithNullAuthentication_ReturnsNull() {
        assertNull(jwtService.generateToken(null));;
    }

    @Test
    void validateToken_WithValidToken_SetsAuthentication() {
        // Arrange
        when(env.getProperty("JWT_SECRET", "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4"))
                .thenReturn(secret);

        String username = "testuser";
        String authorities = "ROLE_USER";
        
        String token = Jwts.builder()
            .issuer("JsnmCFjaN")
            .subject("JWT Token")
            .claim("username", username)
            .claim("authorities", authorities)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
            .signWith(secretKey)
            .compact();

        // Act
        jwtService.validateToken("Bearer " + token);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
            .anyMatch(g -> g.getAuthority().equals(authorities)));
    }

    @Test
    void validateToken_WithInvalidToken_DoesNotSetAuthentication() {
        // Arrange
        SecurityContextHolder.clearContext();
        
        // Act
        jwtService.validateToken("invalid.token.here");
        
        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    void validateToken_WithNullToken_DoesNotSetAuthentication() {
        // Arrange
        SecurityContextHolder.clearContext();
        
        // Act
        jwtService.validateToken(null);
        
        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    void validateToken_WithExpiredToken_ThrowsException() {
        // Arrange
        when(env.getProperty("JWT_SECRET", "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4"))
                .thenReturn(secret);

        String expiredToken = Jwts.builder()
            .issuer("JsnmCFjaN")
            .subject("JWT Token")
            .claim("username", "user")
            .claim("authorities", "ROLE_USER")
            .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) // 2 hours ago
            .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60))   // 1 hour ago
            .signWith(secretKey)
            .compact();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.validateToken("Bearer " + expiredToken);
        });
    }
}
