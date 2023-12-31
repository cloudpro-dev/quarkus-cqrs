package com.example.dto;

import java.math.BigDecimal;

public record BankAccountResponseDTO(String aggregateId, String email, String address, String username, BigDecimal balance) {
}
