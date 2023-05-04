package com.example.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
    }

    public InsufficientFundsException(String id) {
        super("bank account does not have sufficient funds for withdrawal");
    }
}
