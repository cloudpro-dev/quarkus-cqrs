package com.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepositAmountRequestDTO (
    @Min(value = 300, message = "minimum amount is 300") @NotNull BigDecimal amount) {
}
