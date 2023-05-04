package com.example.commands;

import com.example.event.BalanceDepositEvent;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;

public interface BankAccountCommandService {
    Uni<String> handle(CreateBankAccountCommand command);

    Uni<Void> handle(ChangeEmailCommand command);

    Uni<Void> handle(ChangeAddressCommand command);

    Uni<Void> handle(DepositAmountCommand command);

    Uni<Void> handle(WithdrawAmountCommand command);
}
