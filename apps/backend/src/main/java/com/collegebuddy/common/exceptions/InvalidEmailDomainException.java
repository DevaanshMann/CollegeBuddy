package com.collegebuddy.common.exceptions;

public class InvalidEmailDomainException extends RuntimeException {
    public InvalidEmailDomainException(String msg) { super(msg); }
}
