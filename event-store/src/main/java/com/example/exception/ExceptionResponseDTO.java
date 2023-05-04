package com.example.exception;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

public record ExceptionResponseDTO(String message, int status, LocalDateTime timestamp) {
}
