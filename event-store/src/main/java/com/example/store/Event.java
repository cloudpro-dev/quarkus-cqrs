package com.example.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * An event represents a fact that took place in the domain. They are the source of truth; your current state is derived from the events.
 * Events are immutable and represent the business facts.
 * In Event Sourcing, each operation made on the aggregate should result with the new event.
 * It means that we never change or remove anything in the database, and we only append new events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", aggregateId='" + aggregateId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", aggregateType='" + aggregateType + '\'' +
                ", version=" + version + '\'' +
                ", timeStamp=" + timeStamp + '\'' +
                ", data=" + new String(data) + '\'' +
                '}';
    }
}
