package com.example.event;

import com.example.domain.BankAccountAggregate;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmailChangedEvent extends BaseEvent {

    public static final String EMAIL_CHANGED_V1 = "EMAIL_CHANGED_V1";
    public static final String AGGREGATE_TYPE = BankAccountAggregate.AGGREGATE_TYPE;

    private String email;

    @Builder
    public EmailChangedEvent(String aggregateId, String email) {
        super(aggregateId);
        this.email = email;
    }
}
