package com.example.listener;

import com.example.domain.BankAccountDocument;
import com.example.events.*;
import com.example.exception.InvalidEventTypeException;
import com.example.repository.BankAccountMongoRepository;
import com.example.util.SerializerUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class BankAccountMongoProjection implements Projection {

    private final static Logger logger = Logger.getLogger(BankAccountMongoProjection.class);

    @Inject
    BankAccountMongoRepository repository;

    @Incoming(value = "event-store")
    public Uni<Void> process(Message<byte[]> message) {
        logger.infof("(consumer) process events: >>>>> %s", new String(message.getPayload()));
        final Event[] events = SerializerUtils.deserializeEventsFromJsonBytes(message.getPayload());

        if (events.length == 0)
            return Uni.createFrom().voidItem()
                    .onItem().invoke(() -> logger.warn("empty events list"))
                    .onItem().invoke(message::ack)
                    .onFailure().invoke(ex -> logger.error("(process) msg ack exception", ex));

        return Multi.createFrom().iterable(List.of(events))
                .onItem().call(this::when)
                .toUni().replaceWithVoid()
                .onItem().invoke(v -> message.ack())
                .onFailure().invoke(ex -> logger.error("consumer process events aggregateId: %s", events[0].getAggregateId(), ex));

    }

    public Uni<Void> when(Event event) {
        final var aggregateId = event.getAggregateId();
        logger.infof("(when) event aggregateId: >>>>> %s", aggregateId);

        switch(event.getEventType()) {
            case BankAccountCreatedEvent.BANK_ACCOUNT_CREATED_V1 -> {
                return handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BankAccountCreatedEvent.class));
            }
            case AddressChangedEvent.ADDRESS_CHANGED_V1 -> {
                return handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), AddressChangedEvent.class));
            }
            case EmailChangedEvent.EMAIL_CHANGED_V1 -> {
                return handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), EmailChangedEvent.class));
            }
            case BalanceDepositEvent.BALANCE_DEPOSIT_V1 -> {
                return handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BalanceDepositEvent.class));
            }
            case BalanceWithdrawalEvent.BALANCE_WITHDRAWAL_V1 -> {
                return handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BalanceWithdrawalEvent.class));
            }
            default -> {
                return Uni.createFrom().failure(new InvalidEventTypeException(event.getEventType()));
            }
        }
    }

    private Uni<Void> handle(BankAccountCreatedEvent event) {
        logger.infof("(when) BankAccountCreatedEvent: %s, aggregateID: %s", event, event.getAggregateId());

        final var document = BankAccountDocument.builder()
                .aggregateId(event.getAggregateId())
                .email(event.getEmail())
                .address(event.getAddress())
                .userName(event.getUserName())
                .balance(BigDecimal.valueOf(0))
                .build();

        return repository.persist(document)
                .onItem().invoke(result -> logger.infof("persist document result: %s", result))
                .onFailure().invoke(ex -> logger.error("handle BankAccountCreatedEvent persist aggregateID: %s", event.getAggregateId(), ex))
                .replaceWithVoid();
    }

    private Uni<Void> handle(AddressChangedEvent event) {
        logger.infof("(when) AddressChangedEvent: %s, aggregateID: %s", event, event.getAggregateId());

        return repository.findByAggregateId(event.getAggregateId())
                .onFailure().invoke(ex -> logger.error("handle AddressChangedEvent findByAggregateId aggregateId: %s", event.getAggregateId(), ex))
                .chain(result -> {
                    result.setAddress(event.getAddress());
                    return repository.update(result);
                })
                .onFailure().invoke(ex -> logger.error("handle AddressChangeEvent update aggregateId: %s", event.getAggregateId(), ex))
                .onItem().invoke(updatedDocument -> logger.infof("(AddressChangedEvent) updatedDocument: %s", updatedDocument))
                .replaceWithVoid();
    }

    private Uni<Void> handle(EmailChangedEvent event) {
        logger.infof("(when) EmailChangedEvent: %s, aggregateID: %s", event, event.getAggregateId());

        return repository.findByAggregateId(event.getAggregateId())
                .onFailure().invoke(ex -> logger.error("handle EmailChangedEvent findByAggregateId aggregateId: %s", event.getAggregateId(), ex))
                .chain(result -> {
                    result.setEmail(event.getEmail());
                    return repository.update(result);
                })
                .onFailure().invoke(ex -> logger.error("handle EmailChangedEvent update aggregateId: %s", event.getAggregateId(), ex))
                .onItem().invoke(updatedDocument -> logger.infof("(EmailChangedEvent) updatedDocument: %s", updatedDocument))
                .replaceWithVoid();
    }

    private Uni<Void> handle(BalanceDepositEvent event) {
        logger.infof("(when) BalanceDepositEvent: %s, aggregateID: %s", event, event.getAggregateId());

        return repository.findByAggregateId(event.getAggregateId())
                .onFailure().invoke(ex -> logger.error("handle BalanceDepositEvent findByAggregateId aggregateId: %s", event.getAggregateId(), ex))
                .chain(result -> {
                    final var balance = result.getBalance();
                    result.setBalance(balance.add(event.getAmount()));
                    return repository.update(result);
                })
                .onFailure().invoke(ex -> logger.error("handle BalanceDepositEvent update aggregateId: %s", event.getAggregateId(), ex))
                .onItem().invoke(updatedDocument -> logger.infof("(BalanceDepositEvent) updatedDocument: %s", updatedDocument))
                .replaceWithVoid();
    }

    private Uni<Void> handle(BalanceWithdrawalEvent event) {
        logger.infof("(when) BalanceWithdrawalEvent: %s, aggregateID: %s", event, event.getAggregateId());

        return repository.findByAggregateId(event.getAggregateId())
                .onFailure().invoke(ex -> logger.error("handle BalanceWithdrawalEvent findByAggregateId aggregateId: %s", event.getAggregateId(), ex))
                .chain(result -> {
                    final var balance = result.getBalance();
                    result.setBalance(balance.subtract(event.getAmount()));
                    return repository.update(result);
                })
                .onFailure().invoke(ex -> logger.error("handle BalanceWithdrawalEvent update aggregateId: %s", event.getAggregateId(), ex))
                .onItem().invoke(updatedDocument -> logger.infof("(BalanceWithdrawalEvent) updatedDocument: %s", updatedDocument))
                .replaceWithVoid();
    }

}
