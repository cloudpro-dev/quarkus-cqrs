package com.example.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@RegisterForReflection
public class BalanceWithdrawalEvent extends BaseEvent {

    public static final String BALANCE_WITHDRAWAL_V1 = "BALANCE_WITHDRAWAL_V1";

    private BigDecimal amount;

    @Builder
    public BalanceWithdrawalEvent(String aggregateId, BigDecimal amount) {
        super(aggregateId);
        this.amount = amount;
    }
}
