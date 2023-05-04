package com.example.commands;

import com.example.domain.BankAccountAggregate;
import com.example.store.EventStore;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class BankAccountCommandHandler implements BankAccountCommandService {

    private final static Logger logger = Logger.getLogger(BankAccountCommandHandler.class);

    @Inject
    EventStore eventStore;

    @Override
    public Uni<String> handle(CreateBankAccountCommand command) {
        final var aggregate = new BankAccountAggregate(UUID.randomUUID().toString());
        aggregate.createBankAccount(command.email(), command.address(), command.userName());
        return eventStore.save(aggregate)
                .replaceWith(aggregate.getId())
                .onItem().invoke(() -> logger.infof("created bank account: %s", aggregate));
    }

    @Override
    public Uni<Void> handle(ChangeEmailCommand command) {
        return eventStore.load(command.aggregateId(), BankAccountAggregate.class)
                .onItem().transform(aggregate -> {
                    aggregate.changeEmail(command.email());
                    return aggregate;
                })
                .chain(aggregate -> eventStore.save(aggregate))
                .onItem().invoke(() -> logger.infof("email changed: %s, id: %s", command.email(), command.aggregateId()));
    }

    @Override
    public Uni<Void> handle(ChangeAddressCommand command) {
        return eventStore.load(command.aggregateId(), BankAccountAggregate.class)
                .onItem().transform(aggregate -> {
                    aggregate.changeAddress(command.address());
                    return aggregate;
                })
                .chain(aggregate -> eventStore.save(aggregate))
                .onItem().invoke(() -> logger.infof("address changed: %s, id: %s", command.address(), command.aggregateId()));
    }

    @Override
    public Uni<Void> handle(DepositAmountCommand command) {
        return eventStore.load(command.aggregateId(), BankAccountAggregate.class)
                .onItem().transform(aggregate -> {
                    aggregate.depositBalance(command.amount());
                    return aggregate;
                })
                .chain(aggregate -> eventStore.save(aggregate))
                .onItem().invoke(() -> logger.infof("deposited amount: %s, id: %s", command.amount(), command.aggregateId()));
    }

    @Override
    public Uni<Void> handle(WithdrawAmountCommand command) {
        return eventStore.load(command.aggregateId(), BankAccountAggregate.class)
                .onItem().transform(aggregate -> {
                    aggregate.withdrawBalance(command.amount());
                    return aggregate;
                })
                .chain(aggregate -> eventStore.save(aggregate))
                .onItem().invoke(() -> logger.infof("withdrawal amount: %s, id: %s", command.amount(), command.aggregateId()));
    }



}
