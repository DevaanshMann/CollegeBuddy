package com.collegebuddy.common.exceptions;

public class ConnectionRequestNotFoundException extends RuntimeException {
    public ConnectionRequestNotFoundException(String message) {
        super(message);
    }
}
