package com.example.exception;

public class InvalidEventTypeException extends RuntimeException{
    public InvalidEventTypeException() {
    }

    public InvalidEventTypeException(String message) {
        super("invalid event: " + message);
    }
}
