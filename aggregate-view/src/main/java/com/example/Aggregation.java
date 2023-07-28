package com.example;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;

import java.math.BigDecimal;

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
}
