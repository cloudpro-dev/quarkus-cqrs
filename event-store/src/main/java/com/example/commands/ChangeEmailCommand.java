package com.example.commands;

public record ChangeEmailCommand(String aggregateId, String email) {
}
