package com.example.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepositAmountRequestDTO (
    @Min(value = 300, message = "minimum amount is 300") @NotNull BigDecimal amount) {
}
