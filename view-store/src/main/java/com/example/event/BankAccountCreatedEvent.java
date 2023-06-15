package com.example.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BankAccountCreatedEvent extends BaseEvent {

    public static final String BANK_ACCOUNT_CREATED_V1 = "BANK_ACCOUNT_CREATED_V1";
    public static final String AGGREGATE_TYPE = "BankAccountAggregate";

    private String email;
    private String userName;
    private String address;

    @Builder
    public BankAccountCreatedEvent(String aggregateId, String email, String userName, String address) {
        super(aggregateId);
        this.email = email;
        this.userName = userName;
        this.address = address;
    }
}
