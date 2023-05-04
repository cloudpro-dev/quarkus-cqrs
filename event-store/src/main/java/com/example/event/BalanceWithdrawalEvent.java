package com.example.event;

import com.example.domain.BankAccountAggregate;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceWithdrawalEvent extends BaseEvent {

    public static final String BALANCE_WITHDRAWAL_V1 = "BALANCE_WITHDRAWAL_V1";
    public static final String AGGREGATE_TYPE = BankAccountAggregate.AGGREGATE_TYPE;

    private BigDecimal amount;

    @Builder
    public BalanceWithdrawalEvent(String aggregateId, BigDecimal amount) {
        super(aggregateId);
        this.amount = amount;
    }
}
