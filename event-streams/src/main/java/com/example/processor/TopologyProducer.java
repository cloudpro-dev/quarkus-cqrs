package com.example.processor;

import com.example.domain.Aggregation;
import com.example.domain.Event;
import com.example.util.SerializerUtils;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.jboss.logging.Logger;

import java.util.Arrays;

@ApplicationScoped
public class TopologyProducer {

    private final static Logger logger = Logger.getLogger(TopologyProducer.class);

    static final String AGGREGATE_STORE = "aggregates-store";

    static final String EVENTS_TOPIC = "event-store";

    static final String EVENT_AGGREGATE_TOPIC = "event-store-aggregated";

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(AGGREGATE_STORE);

        ObjectMapperSerde<Event> eventSerde = new ObjectMapperSerde<>(Event.class);
        ObjectMapperSerde<Aggregation> aggregationSerde = new ObjectMapperSerde<>(Aggregation.class);

        KeyValueMapper<String, byte[], KeyValue<String, Event[]>> mapper = (k, v) -> new KeyValue<>(k, SerializerUtils.deserializeEventsFromJsonBytes(v));

        builder
                .stream(
                        EVENTS_TOPIC,
                        Consumed.with(Serdes.String(), Serdes.ByteArray())
                )
                .peek((k, v) -> logger.debugf("Observed bytes: %s", v))
                .map(mapper)
                .peek((k, v) -> logger.debugf("Mapped events: %s", v))
                .flatMapValues(events -> Arrays.asList(events))
                .peek((k, v) -> logger.debugf("Transformed event: %s", v))
                .groupBy((k, v) -> {
                    logger.infof("Group by key=%s, value=%s", k, v);
                    return v.getAggregateType();
                }, Grouped.with(Serdes.String(), eventSerde))
                .aggregate(Aggregation::new,
                        (key, value, aggregation) -> aggregation.updateFrom(value),
                        Materialized.<String, Aggregation> as(storeSupplier)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(aggregationSerde))
                .toStream()
                .peek((k, v) -> logger.debugf("Stream event: %s", v))
                .to(
                        EVENT_AGGREGATE_TOPIC,
                        Produced.with(Serdes.String(), aggregationSerde)
                );

        return builder.build();
    }



}
