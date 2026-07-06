package com.centria.cabbooking.exception;

/** Thrown when a user role/ownership/status check fails a backend-level permission gate. */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
