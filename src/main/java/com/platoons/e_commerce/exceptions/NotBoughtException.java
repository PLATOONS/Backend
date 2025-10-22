package com.platoons.e_commerce.exceptions;

public class NotBoughtException extends RuntimeException {
    public NotBoughtException(String message) {
        super(message);
    }
}
