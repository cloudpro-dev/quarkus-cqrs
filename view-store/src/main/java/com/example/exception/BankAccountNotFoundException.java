package com.example.exception;

public class BankAccountNotFoundException extends RuntimeException {

    public BankAccountNotFoundException() {
    }

    public BankAccountNotFoundException(String id) {
        super("bank account not found id: " + id);
    }
}