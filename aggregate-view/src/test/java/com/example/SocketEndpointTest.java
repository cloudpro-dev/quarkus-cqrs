package com.example;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.websocket.*;
import net.mguenther.kafka.junit.ExternalKafkaCluster;
import net.mguenther.kafka.junit.SendValues;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@QuarkusTest
public class SocketEndpointTest {

    private static final LinkedBlockingDeque<String> MESSAGES = new LinkedBlockingDeque<>();

    private static final String AGGREGATE_TOPIC = "event-store-aggregated";

    @TestHTTPResource("/bankaccount-aggregates/rob")
    URI uri;

    @Test
    public void testWebsocket() throws Exception {
        ExternalKafkaCluster kafka = ExternalKafkaCluster.at("localhost:29092");

        String payload = "{\"accountTotal\":20,\"countDeposits\":15,\"countWithdrawals\":9,\"avgDeposit\":500.00," +
                "\"avgWithdrawal\":100.00,\"avgBalance\":471.43,\"totalDeposits\":7500.00,\"totalWithdrawals\":900.00}";

        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));

            kafka.send(
                    SendValues.to(AGGREGATE_TOPIC, payload)
                            .with(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class));

            Assertions.assertEquals(payload, MESSAGES.poll(10, TimeUnit.SECONDS));
        }
    }

    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(Session session) {
            MESSAGES.add("CONNECT");
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            session.getAsyncRemote().sendText("_ready_");
        }

        @OnMessage
        void message(String msg) {
            MESSAGES.add(msg);
        }

    }

}
