package com.example.processor;

import com.example.domain.Aggregation;
import com.example.domain.Event;
import com.example.event.BalanceDepositEvent;
import com.example.event.BalanceWithdrawalEvent;
import com.example.event.BankAccountCreatedEvent;
import com.example.util.SerializerUtils;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.test.TestRecord;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

import static com.example.event.BalanceDepositEvent.BALANCE_DEPOSIT_V1;
import static com.example.event.BalanceWithdrawalEvent.BALANCE_WITHDRAWAL_V1;
import static com.example.event.BankAccountCreatedEvent.BANK_ACCOUNT_CREATED_V1;

/**
 * Testing of the Topology without a broker, using TopologyTestDriver
 */
@QuarkusTest
public class TopologyProducerTest {

    static final String IN_TOPIC = "event-store";

    static final String OUT_TOPIC = "event-store-aggregated";

    static final String STORE_NAME = "aggregates-store";

    @Inject
    Topology topology;

    TopologyTestDriver testDriver;

    TestInputTopic<String, byte[]> eventsTopic;

    TestOutputTopic<String, String> aggregatedEventsTopic;

    @BeforeEach
    public void setUp(){
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "testApplicationId");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        testDriver = new TopologyTestDriver(topology, config);

        eventsTopic = testDriver.createInputTopic(IN_TOPIC, new StringSerializer(), new ByteArraySerializer());

        aggregatedEventsTopic = testDriver.createOutputTopic(OUT_TOPIC, new StringDeserializer(),
                new StringDeserializer());
    }

    @AfterEach
    public void tearDown(){
        testDriver.getTimestampedKeyValueStore(STORE_NAME).flush();
        testDriver.close();
    }

    @Test
    public void testBankAccountCreatedEvent() {
        String aggregateId = UUID.randomUUID().toString();

        // bank account created tests

        BankAccountCreatedEvent createEvent = new BankAccountCreatedEvent(aggregateId,
                "test@test.com", "testuser", "1 Test Lane, Test Town, Testington, TT1 1TT");
        Event event = createEvent(BANK_ACCOUNT_CREATED_V1, aggregateId, SerializerUtils.serializeToJsonBytes(createEvent));
        eventsTopic.pipeInput(null, SerializerUtils.serializeToJsonBytes(Collections.singletonList(event)));
        eventsTopic.pipeInput(null, SerializerUtils.serializeToJsonBytes(Collections.singletonList(event)));

        List<TestRecord<String, String>> results = aggregatedEventsTopic.readRecordsToList();
        Aggregation aggregate = SerializerUtils.deserializeFromString(results.get(1).getValue(), Aggregation.class);

        // assert it has the correct account total
        Assertions.assertEquals(2, aggregate.accountTotal);

        // deposit tests

        BalanceDepositEvent depositEvent = new BalanceDepositEvent(aggregateId, new BigDecimal("100"));
        event = createEvent(BALANCE_DEPOSIT_V1, aggregateId, SerializerUtils.serializeToJsonBytes(depositEvent));
        eventsTopic.pipeInput(null, SerializerUtils.serializeToJsonBytes(Collections.singletonList(event)));
        eventsTopic.pipeInput(null, SerializerUtils.serializeToJsonBytes(Collections.singletonList(event)));
        eventsTopic.pipeInput(null, SerializerUtils.serializeToJsonBytes(Collections.singletonList(event)));

        results = aggregatedEventsTopic.readRecordsToList();
        aggregate = SerializerUtils.deserializeFromString(results.get(2).getValue(), Aggregation.class); // last record

        // assert it has the correct deposit totals
        Assertions.assertEquals(new BigDecimal("300"), aggregate.totalDeposits);
        Assertions.assertEquals(new BigDecimal("150"), aggregate.avgBalance);
        Assertions.assertEquals(new BigDecimal("100"), aggregate.avgDeposit);

        // withdrawal tests

        BalanceWithdrawalEvent withdrawalEvent = new BalanceWithdrawalEvent(aggregateId, new BigDecimal("50"));
        event = createEvent(BALANCE_WITHDRAWAL_V1, aggregateId, SerializerUtils.serializeToJsonBytes(withdrawalEvent));
        eventsTopic.pipeInput(null, SerializerUtils.serializeToJsonBytes(Collections.singletonList(event)));
        eventsTopic.pipeInput(null, SerializerUtils.serializeToJsonBytes(Collections.singletonList(event)));
        eventsTopic.pipeInput(null, SerializerUtils.serializeToJsonBytes(Collections.singletonList(event)));

        results = aggregatedEventsTopic.readRecordsToList();
        aggregate = SerializerUtils.deserializeFromString(results.get(2).getValue(), Aggregation.class); // last record

        // assert it has the correct withdrawal totals
        Assertions.assertEquals(new BigDecimal("150"), aggregate.totalWithdrawals);
        Assertions.assertEquals(new BigDecimal("75"), aggregate.avgBalance); // TODO is this correct?
        Assertions.assertEquals(new BigDecimal("50"), aggregate.avgWithdrawal);
    }

    private Event createEvent(String eventType, String aggregateId, byte[] data) {
        return Event.builder()
                .id(UUID.randomUUID())
                .eventType(eventType)
                .aggregateType("BankAccountAggregate")
                .aggregateId(aggregateId)
                .version(1)
                .timeStamp(ZonedDateTime.now())
                .data(data).build();
    }

}
