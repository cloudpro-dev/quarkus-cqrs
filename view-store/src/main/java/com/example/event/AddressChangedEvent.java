package com.example.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddressChangedEvent extends BaseEvent {

    public static final String ADDRESS_CHANGED_V1 = "ADDRESS_CHANGED_V1";
    public static final String AGGREGATE_TYPE = "BankAccountAggregate";

    private String address;

    @Builder
    public AddressChangedEvent(String aggregateId, String address) {
        super(aggregateId);
        this.address = address;
    }
}
