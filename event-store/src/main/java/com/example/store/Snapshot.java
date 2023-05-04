package com.example.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Snapshot {
    private UUID id;
    private String aggregateType;
    private String aggregateId;
    private long version;
    private byte[] data;
    private byte[] metaData;
    private LocalDateTime timeStamp;

    @Override
    public String toString() {
        return "Snapshot{" +
                "id=" + id +
                ", aggregateId='" + aggregateId + '\'' +
                ", aggregateType='" + aggregateType + '\'' +
                ", data=" + data.length + " bytes" +
                ", metaData=" + metaData.length + " bytes" +
                ", version=" + version +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
