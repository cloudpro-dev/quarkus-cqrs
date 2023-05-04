package com.example.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawAmountRequestDTO(
        @Min(value = 10, message = "minimum withdrawal is 10") @NotNull BigDecimal amount
) {
}
