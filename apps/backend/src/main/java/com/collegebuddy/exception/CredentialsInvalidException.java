package com.collegebuddy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CredentialsInvalidException extends RuntimeException {
    public CredentialsInvalidException(String message) { super(message); }
}