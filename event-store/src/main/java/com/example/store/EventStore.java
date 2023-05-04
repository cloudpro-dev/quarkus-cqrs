package com.example.store;

import com.example.domain.AggregateRoot;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlConnection;

import java.util.List;

/**
 * Event store is a key element of a system. Each change that took place in the domain is recorded in the database.
 * It is specifically designed to store the history of changes, the state is represented by the append-only log of events.
 * The events are immutable: they cannot be changed.
 * Implementation of AggregateStore is Load, Save, and Exists methods.
 * Load and Save accept aggregate then load or apply events using EventStoreDB client.
 */
public interface EventStore {

    /**
     * Persists aggregates by saving the history of changes, handling concurrency,
     * when you retrieve a stream from EventStoreDB, you take note of the current version number,
     * then when you save it back you can determine if somebody else has modified the record in the meantime.
     * @param aggregate
     * @return
     * @param <T>
     */
    <T extends AggregateRoot> Uni<Void> save(final T aggregate);

    /**
     * Find out the stream name for an aggregate, read all the events from the aggregate stream,
     * loop through all the events, and call the RaiseEvent handler for each of them.
     * @param aggregateId
     * @param aggregateType
     * @return
     * @param <T>
     */
    <T extends AggregateRoot> Uni<T> load(final String aggregateId, final Class<T> aggregateType);

    Uni<Boolean> exists(final String aggregateId);

    Uni<RowSet<Row>> saveEvents(SqlConnection client, final List<Event> events);

    Uni<RowSet<Event>> loadEvents(final String aggregateId, long version);

}
