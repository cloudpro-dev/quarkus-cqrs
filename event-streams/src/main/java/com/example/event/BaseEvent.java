package com.example.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@RegisterForReflection
public abstract class BaseEvent {
    protected String aggregateId;

    public BaseEvent(String aggregateId) {
        Objects.requireNonNull(aggregateId);
        if(aggregateId.isBlank()) throw new RuntimeException("BaseEvent aggregateId is required");
        this.aggregateId = aggregateId;
    }
}
