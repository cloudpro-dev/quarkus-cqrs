package com.example.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
public class Event {
    private UUID id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private long version;
    private byte[] data;
    private byte[] metaData;
    private ZonedDateTime timeStamp;

    public Event(String eventType, String aggregateType) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.timeStamp = ZonedDateTime.now();
    }
}

