package com.collegebuddy.common.exceptions;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String msg) { super(msg); }
}
