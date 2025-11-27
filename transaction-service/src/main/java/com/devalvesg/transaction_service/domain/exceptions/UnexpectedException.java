package com.devalvesg.transaction_service.domain.exceptions;

public class UnexpectedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "An unexpected error occurred";

    public UnexpectedException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
}