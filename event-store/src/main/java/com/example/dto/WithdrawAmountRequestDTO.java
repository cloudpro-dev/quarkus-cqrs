package com.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawAmountRequestDTO(
        @Min(value = 10, message = "minimum withdrawal is 10") @NotNull BigDecimal amount
) {
}
