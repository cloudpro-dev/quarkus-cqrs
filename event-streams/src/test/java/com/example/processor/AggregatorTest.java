package com.example.processor;

import com.example.domain.Aggregation;
import com.example.domain.Event;
import com.example.event.BalanceDepositEvent;
import com.example.event.BalanceWithdrawalEvent;
import com.example.event.BankAccountCreatedEvent;
import com.example.util.SerializerUtils;
import io.quarkus.test.junit.QuarkusTest;
import net.mguenther.kafka.junit.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

@QuarkusTest
public class AggregatorTest {

    @BeforeEach
    public void setUp() throws Exception {
        // delete Kafka Streams state between runs
        deleteDirectory(new File("/tmp/event-store-aggregator"));
    }

    @Test
    public void testAggregateEvents() throws InterruptedException {
        ExternalKafkaCluster kafka = ExternalKafkaCluster.at("localhost:29094"); // fixed port in config
        kafka.createTopic(TopicConfig.withName("event-store"));

        Event createEvent = accountCreateEvent();
        Event depositEvent = accountDepositEvent(createEvent.getAggregateId());
        Event withdrawalEvent = accountWithdrawalEvent(createEvent.getAggregateId());

        byte[] createEventBytes = SerializerUtils.serializeToJsonBytes(Collections.singletonList(createEvent).toArray(new Event[]{}));
        byte[] depositEventBytes = SerializerUtils.serializeToJsonBytes(Collections.singletonList(depositEvent).toArray(new Event[]{}));
        byte[] withdrawalEventBytes = SerializerUtils.serializeToJsonBytes(Collections.singletonList(withdrawalEvent).toArray(new Event[]{}));

        String aggregateId = createEvent.getAggregateId();
        List<KeyValue<String, byte[]>> records = new ArrayList<>();
        records.add(new KeyValue<>(aggregateId, createEventBytes));
        records.add(new KeyValue<>(aggregateId, createEventBytes));
        records.add(new KeyValue<>(aggregateId, depositEventBytes));
        records.add(new KeyValue<>(aggregateId, depositEventBytes));
        records.add(new KeyValue<>(aggregateId, withdrawalEventBytes));

        kafka.send(SendKeyValues.to("event-store", records)
                .with(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class));

        // wait for the 5 events to be delivered
        kafka.observeValues(ObserveKeyValues.on("event-store", 5));

        // wait for 2 aggregation results to arrive
        List<String> aggregateResults = kafka.observeValues(ObserveKeyValues.on("event-store-aggregated", 1));
        Aggregation aggregation = SerializerUtils.deserializeFromString(aggregateResults.get(0), Aggregation.class);

        // assert the aggregate values
        Assertions.assertEquals(2, aggregation.accountTotal);
        Assertions.assertEquals(new BigDecimal("1000.00"), aggregation.totalDeposits);
        Assertions.assertEquals(new BigDecimal("200.00"), aggregation.totalWithdrawals);
        Assertions.assertEquals(new BigDecimal("500.00"), aggregation.avgDeposit);
        Assertions.assertEquals(new BigDecimal("200.00"), aggregation.avgWithdrawal);
        Assertions.assertEquals(new BigDecimal("400.00"), aggregation.avgBalance);
    }

    Event accountCreateEvent() {
        UUID eventId = UUID.randomUUID();
        String aggregateId = UUID.randomUUID().toString();

        BankAccountCreatedEvent data = BankAccountCreatedEvent.builder()
                .aggregateId(aggregateId)
                .email("test@test.com")
                .address("Test House, Test Lane")
                .userName("testuser1")
                .build();

        byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);

        return Event.builder()
                .id(eventId)
                .aggregateId(aggregateId)
                .aggregateType("BankAccountAggregate")
                .eventType("BANK_ACCOUNT_CREATED_V1")
                .version(1)
                .data(Objects.isNull(dataBytes) ? new byte[]{} : dataBytes)
                .metaData(new byte[]{})
                .timeStamp(ZonedDateTime.now())
                .build();
    }

    Event accountDepositEvent(String aggregateId) {
        BalanceDepositEvent data = BalanceDepositEvent.builder()
                .aggregateId(aggregateId)
                .amount(new BigDecimal("500.00"))
                .build();

        byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);

        return Event.builder()
                .id(UUID.randomUUID())
                .aggregateId(aggregateId)
                .aggregateType("BankAccountAggregate")
                .eventType("BALANCE_DEPOSIT_V1")
                .version(2)
                .data(Objects.isNull(dataBytes) ? new byte[]{} : dataBytes)
                .metaData(new byte[]{})
                .timeStamp(ZonedDateTime.now())
                .build();
    }

    Event accountWithdrawalEvent(String aggregateId) {
        BalanceWithdrawalEvent data = BalanceWithdrawalEvent.builder()
                .aggregateId(aggregateId)
                .amount(new BigDecimal("200.00"))
                .build();

        byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);

        return Event.builder()
                .id(UUID.randomUUID())
                .aggregateId(aggregateId)
                .aggregateType("BankAccountAggregate")
                .eventType("BALANCE_WITHDRAWAL_V1")
                .version(2)
                .data(Objects.isNull(dataBytes) ? new byte[]{} : dataBytes)
                .metaData(new byte[]{})
                .timeStamp(ZonedDateTime.now())
                .build();
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

}
