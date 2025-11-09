package com.collegebuddy.common.exceptions;

public class ConnectionAlreadyExistsException extends RuntimeException {
    public ConnectionAlreadyExistsException(String message) {
        super(message);
    }
}
