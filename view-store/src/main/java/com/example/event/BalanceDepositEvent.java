package com.example.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceDepositEvent extends BaseEvent {

    public static final String BALANCE_DEPOSIT_V1 = "BALANCE_DEPOSIT_V1";
    public static final String AGGREGATE_TYPE = "BankAccountAggregate";

    private BigDecimal amount;

    @Builder
    public BalanceDepositEvent(String aggregateId, BigDecimal amount) {
        super(aggregateId);
        this.amount = amount;
    }
}
