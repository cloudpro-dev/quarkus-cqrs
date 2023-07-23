package com.example.domain;

import com.example.event.BankAccountCreatedEvent;
import com.example.event.BalanceDepositEvent;
import com.example.event.BalanceWithdrawalEvent;
import com.example.util.SerializerUtils;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RegisterForReflection
@ToString
public class Aggregation {
    public long accountTotal;
    public long countDeposits;
    public long countWithdrawals;
    public BigDecimal avgDeposit = new BigDecimal(0);
    public BigDecimal avgWithdrawal = new BigDecimal(0);
    public BigDecimal avgBalance = new BigDecimal(0);
    public BigDecimal totalDeposits = new BigDecimal(0);
    public BigDecimal totalWithdrawals = new BigDecimal(0);

    public Aggregation updateFrom(Event evt) {
        switch(evt.getEventType()) {
            case BankAccountCreatedEvent.BANK_ACCOUNT_CREATED_V1 -> {
                accountTotal++;
                return this;
            }
            case BalanceDepositEvent.BALANCE_DEPOSIT_V1 -> {
                BigDecimal amount = SerializerUtils.deserializeFromJsonBytes(evt.getData(), BalanceDepositEvent.class).getAmount();
                totalDeposits = totalDeposits.add(amount);
                avgDeposit = totalDeposits
                        .divide(BigDecimal.valueOf(++countDeposits), RoundingMode.CEILING);
                avgBalance = totalDeposits
                        .subtract(totalWithdrawals)
                        .divide(BigDecimal.valueOf(accountTotal), RoundingMode.CEILING);
                return this;
            }
            case BalanceWithdrawalEvent.BALANCE_WITHDRAWAL_V1 -> {
                BigDecimal amount = SerializerUtils.deserializeFromJsonBytes(evt.getData(), BalanceWithdrawalEvent.class).getAmount();
                totalWithdrawals = totalWithdrawals.add(amount);
                avgWithdrawal = totalWithdrawals
                        .divide(BigDecimal.valueOf(++countWithdrawals), RoundingMode.CEILING);
                avgBalance = totalDeposits
                        .subtract(totalWithdrawals)
                        .divide(BigDecimal.valueOf(accountTotal), RoundingMode.CEILING);
                return this;
            }
            default -> {
                throw new RuntimeException("Invalid event type " + evt.getEventType());
            }
        }
    }
}
