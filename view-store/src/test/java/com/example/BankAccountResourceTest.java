package com.example;

import com.example.event.BalanceDepositEvent;
import com.example.event.BankAccountCreatedEvent;
import com.example.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.quarkus.test.junit.QuarkusTest;
import net.mguenther.kafka.junit.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

@QuarkusTest
public class BankAccountResourceTest {

    private static final Logger LOG = Logger.getLogger(BankAccountResourceTest.class);

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new ParameterNamesModule())
            .addModule(new Jdk8Module())
            .addModule(new JavaTimeModule())
            .build();

    @Test
    public void testAggregateEvents() throws InterruptedException {
        ExternalKafkaCluster kafka = ExternalKafkaCluster.at("localhost:9098"); // fixed port in config

        Event createEvent = accountCreateEvent();
        Event depositEvent = accountDepositEvent(createEvent);

        byte[] createEventBytes = serializeToJsonBytes(Collections.singletonList(createEvent).toArray(new Event[]{}));
        byte[] depositEventBytes = serializeToJsonBytes(Collections.singletonList(depositEvent).toArray(new Event[]{}));

        kafka.send(
                SendValues.to("event-store", createEventBytes)
                        .with(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.ByteArraySerializer.class));

        kafka.send(
                SendValues.to("event-store", depositEventBytes)
                        .with(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.ByteArraySerializer.class));

        // wait for 2 messages to arrive before proceeding
        kafka.observeValues(ObserveKeyValues.on("event-store", 2));

        // read all balances
        given()
                .when()
                .contentType("application/json")
                .get("/api/v1/bank/balance?page=0&size=5")
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].balance", equalTo(500.00f));

        // read user balance
        given()
                .when()
                .contentType("application/json")
                .get("/api/v1/bank/" + createEvent.getAggregateId())
                .then()
                .assertThat()
                .statusCode(200)
                .body("balance", equalTo(500.00f));
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

        byte[] dataBytes = serializeToJsonBytes(data);

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

    Event accountDepositEvent(Event createEvent) {
        BalanceDepositEvent data = BalanceDepositEvent.builder()
                .aggregateId(createEvent.getAggregateId())
                .amount(new BigDecimal("500.00"))
                .build();

        byte[] dataBytes = serializeToJsonBytes(data);

        return Event.builder()
                .id(UUID.randomUUID())
                .aggregateId(createEvent.getAggregateId())
                .aggregateType("BankAccountAggregate")
                .eventType("BALANCE_DEPOSIT_V1")
                .version(2)
                .data(Objects.isNull(dataBytes) ? new byte[]{} : dataBytes)
                .metaData(new byte[]{})
                .timeStamp(ZonedDateTime.now())
                .build();
    }

    byte[] serializeToJsonBytes(final Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
