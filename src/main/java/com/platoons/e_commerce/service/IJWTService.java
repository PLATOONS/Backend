package com.platoons.e_commerce.service;

import org.springframework.security.core.Authentication;

public interface IJWTService {
    void validateToken(String jwt);
    String generateToken(Authentication authentication);
}
