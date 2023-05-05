package com.example.store;

import com.example.util.SerializerUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class DefaultEventBus implements EventBus {
    private final static Logger logger = Logger.getLogger(DefaultEventBus.class);

    private static final int PUBLISH_TIMEOUT = 1000;
    private static final int BACKOFF_TIMEOUT = 300;
    private static final int RETRY_COUNT = 3;

    @Inject
    @Channel("event-store")
    MutinyEmitter<byte[]> emitter;

    @Override
    public Uni<Void> publish(List<Event> events) {
        final byte[] eventsBytes = SerializerUtils.serializeToJsonBytes(events.toArray(new Event[]{}));
        return emitter.send(eventsBytes)
                .ifNoItem().after(Duration.ofMillis(PUBLISH_TIMEOUT)).fail()
                .onFailure().invoke(Throwable::printStackTrace)
                .onFailure().retry().withBackOff(Duration.of(BACKOFF_TIMEOUT, ChronoUnit.MILLIS)).atMost(RETRY_COUNT)
                .onItem().invoke(msg -> logger.infof("publish topic: %s, value: %s", "event-store", new String(eventsBytes)))
                .replaceWithVoid();
    }
}
