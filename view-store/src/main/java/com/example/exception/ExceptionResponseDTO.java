package com.example.exception;

import java.time.LocalDateTime;

public record ExceptionResponseDTO(String message, int status, LocalDateTime timestamp) {
}
