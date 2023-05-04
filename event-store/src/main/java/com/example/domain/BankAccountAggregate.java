package com.example.domain;

import com.example.event.*;
import com.example.exception.InsufficientFundsException;
import com.example.store.Event;
import com.example.util.SerializerUtils;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BankAccountAggregate extends AggregateRoot {

    public static final String AGGREGATE_TYPE = "BankAccountAggregate";

    private String email;
    private String userName;
    private String address;
    private BigDecimal balance;

    public BankAccountAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case BankAccountCreatedEvent.BANK_ACCOUNT_CREATED_V1 -> {
                handleEvent(SerializerUtils.deserializeFromJsonBytes(event.getData(), BankAccountCreatedEvent.class));
            }
            case EmailChangedEvent.EMAIL_CHANGED_V1 -> {
                handleEvent(SerializerUtils.deserializeFromJsonBytes(event.getData(), EmailChangedEvent.class));
            }
            case AddressChangedEvent.ADDRESS_CHANGED_V1 -> {
                handleEvent(SerializerUtils.deserializeFromJsonBytes(event.getData(), AddressChangedEvent.class));
            }
            case BalanceDepositEvent.BALANCE_DEPOSIT_V1 -> {
                handleEvent(SerializerUtils.deserializeFromJsonBytes(event.getData(), BalanceDepositEvent.class));
            }
            case BalanceWithdrawalEvent.BALANCE_WITHDRAWAL_V1 -> {
                handleEvent(SerializerUtils.deserializeFromJsonBytes(event.getData(), BalanceWithdrawalEvent.class));
            }
        }
    }

    private void handleEvent(final BankAccountCreatedEvent event) {
        this.email = event.getEmail();
        this.userName = event.getUserName();
        this.address = event.getAddress();
        this.balance = BigDecimal.valueOf(0);
    }


    private void handleEvent(final EmailChangedEvent event) {
        this.email = event.getEmail();
    }

    private void handleEvent(final AddressChangedEvent event) {
        this.address = event.getAddress();
    }

    private void handleEvent(final BalanceDepositEvent event) {
        this.balance = this.balance.add(event.getAmount());
    }

    private void handleEvent(final BalanceWithdrawalEvent event) {
        // validate account has sufficient funds
        if(BigDecimal.ZERO.compareTo(this.balance.subtract(event.getAmount())) >= 0) {
            throw new InsufficientFundsException(event.getAggregateId());
        }
        this.balance = this.balance.subtract(event.getAmount());
    }


    public void createBankAccount(String email, String address, String userName) {
        final var data = BankAccountCreatedEvent.builder()
                .aggregateId(id)
                .email(email)
                .address(address)
                .userName(userName)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BankAccountCreatedEvent.BANK_ACCOUNT_CREATED_V1, dataBytes, null);
        this.apply(event);
    }

    public void changeEmail(String email) {
        final var data = EmailChangedEvent.builder().aggregateId(id).email(email).build();
        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(EmailChangedEvent.EMAIL_CHANGED_V1, dataBytes, null);
        this.apply(event);
    }

    public void changeAddress(String address) {
        final var data = AddressChangedEvent.builder().aggregateId(id).address(address).build();
        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(AddressChangedEvent.ADDRESS_CHANGED_V1, dataBytes, null);
        this.apply(event);
    }

    public void depositBalance(BigDecimal amount) {
        final var data = BalanceDepositEvent.builder().aggregateId(id).amount(amount).build();
        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BalanceDepositEvent.BALANCE_DEPOSIT_V1, dataBytes, null);
        this.apply(event);
    }
    public void withdrawBalance(BigDecimal amount) {
        final var data = BalanceWithdrawalEvent.builder().aggregateId(id).amount(amount).build();
        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BalanceWithdrawalEvent.BALANCE_WITHDRAWAL_V1, dataBytes, null);
        this.apply(event);
    }


    @Override
    public String toString() {
        return "BankAccountAggregate{" +
                "email='" + email + '\'' +
                ", userName='" + userName + '\'' +
                ", address='" + address + '\'' +
                ", balance=" + balance +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", version=" + version +
                ", changes=" + changes.size() +
                '}';
    }
}
