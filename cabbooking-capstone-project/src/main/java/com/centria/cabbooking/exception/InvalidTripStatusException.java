package com.centria.cabbooking.exception;

/** Thrown whenever a requested trip state transition is not allowed by the state machine. */
public class InvalidTripStatusException extends RuntimeException {
    public InvalidTripStatusException(String message) {
        super(message);
    }
}
