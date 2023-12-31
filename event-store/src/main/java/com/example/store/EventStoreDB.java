package com.example.store;

import com.example.domain.AggregateRoot;
import com.example.store.exception.BankAccountNotFoundException;
import com.example.util.SerializerUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.mutiny.sqlclient.Tuple;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.store.Constants.*;


@ApplicationScoped
public class EventStoreDB implements EventStore {

    private final static Logger logger = Logger.getLogger(EventStoreDB.class);

    private final int SNAPSHOT_FREQUENCY = 4;

    private final static String SAVE_EVENTS_QUERY = "INSERT INTO events (aggregate_id, aggregate_type, event_type, data, metadata, version, timestamp) " +
            "VALUES ($1, $2, $3, $4, $5, $6, now())";
    private final static String HANDLE_CONCURRENCY_QUERY = "SELECT aggregate_id FROM events e WHERE e.aggregate_id = $1 LIMIT 1 FOR UPDATE";

    private final static String LOAD_EVENTS_QUERY = "SELECT event_id ,aggregate_id, aggregate_type, event_type, data, metadata, version, timestamp" +
            " FROM events e WHERE e.aggregate_id = $1 and e.version > $2 ORDER BY e.version";

    private final static String EXISTS_QUERY = "SELECT e.aggregate_id FROM events e WHERE e.aggregate_id = $1 LIMIT 1";

    private final static String LOAD_SNAPSHOT_QUERY = "SELECT snapshot_id, aggregate_id, aggregate_type, data, metadata, version, timestamp FROM snapshots s WHERE s.aggregate_id = $1";

    private final static String SAVE_SNAPSHOT_QUERY = "INSERT INTO snapshots (aggregate_id, aggregate_type, data, metadata, version, timestamp) " +
            "VALUES ($1, $2, $3, $4, $5, now()) " +
            "ON CONFLICT (aggregate_id) " +
            "DO UPDATE SET data = $3, version = $5, timestamp = now()";

    @Inject
    PgPool pgPool;

    @Inject
    EventBus eventBus;

    @Override
    public <T extends AggregateRoot> Uni<Void> save(T aggregate) {
        // TODO prove all actions are rolled back in event of failure of event bus emission
        final List<Event> changes = new ArrayList<>(aggregate.getChanges());
        return pgPool.withTransaction(client -> handleConcurrency(client, aggregate.getId())
                .chain(r -> saveEvents(client, aggregate.getChanges()))
                .chain(r -> aggregate.getVersion() % SNAPSHOT_FREQUENCY == 0 ? saveSnapshot(client, aggregate) : Uni.createFrom().item(r))
                .chain(a -> eventBus.publish(changes))
                .onFailure().invoke(ex -> logger.error("(save) eventBus.publish ex", ex))
                .onItem().invoke(success -> logger.infof("save success")));
    }

    @Override
    public <T extends AggregateRoot> Uni<T> load(String aggregateId, Class<T> aggregateType) {
        return pgPool.withTransaction(client -> getSnapshot(client, aggregateId)
                        .onItem().transform(snapshot -> getSnapshotFromClass(snapshot, aggregateId, aggregateType)))
                .chain(a -> this.loadEvents(a.getId(), a.getVersion())
                .chain(events -> raiseAggregateEvents(a, events)));
    }

    @Override
    public Uni<Boolean> exists(String aggregateId) {
        return pgPool.preparedQuery(EXISTS_QUERY).execute(Tuple.of(aggregateId))
                .map(m -> m.rowCount() > 0)
                .onFailure().invoke(ex -> logger.error("(exists) aggregateId: %s, ex:", aggregateId, ex));
    }

    @Override
    public Uni<RowSet<Row>> saveEvents(SqlConnection client, List<Event> events) {
        final List<Tuple> tupleList = events.stream().map(event -> Tuple.of(
                event.getAggregateId(),
                event.getAggregateType(),
                event.getEventType(),
                Objects.isNull(event.getData()) ? new byte[]{} : event.getData(),
                Objects.isNull(event.getMetaData()) ? new byte[]{} : event.getMetaData(),
                event.getVersion()
        )).toList();

        if(tupleList.size() == 1) {
            // single save
            return client.preparedQuery(SAVE_EVENTS_QUERY).execute(tupleList.get(0))
                    .onFailure().invoke(ex -> logger.error("(saveEvents) preparedQuery ex:", ex))
                    .onItem().invoke(result -> logger.infof("(saveEvents) execute result: %s", result.rowCount()));
        }

        // batch save
        return client.preparedQuery(SAVE_EVENTS_QUERY).executeBatch(tupleList)
                .onFailure().invoke(ex -> logger.error("(executeBatch) preparedQuery ex:", ex))
                .onItem().invoke(result -> logger.infof("(saveEvents) execute result: %s", result.rowCount()));
    }

    @Override
    public Uni<RowSet<Event>> loadEvents(String aggregateId, long version) {
        return pgPool.preparedQuery(LOAD_EVENTS_QUERY)
                .mapping(EventStoreDB::eventFromRow)
                .execute(Tuple.of(aggregateId, version))
                .onFailure().invoke(ex -> logger.error("(loadEvents) preparedQuery ex:", ex));
    }

    // private methods

    private Uni<Snapshot> getSnapshot(SqlConnection client, String aggregateId) {
        return client.preparedQuery(LOAD_SNAPSHOT_QUERY).mapping(EventStoreDB::snapshotFromRow)
                .execute(Tuple.of(aggregateId))
                .onFailure().invoke(ex -> logger.error("(getSnapshot) preparedQuery ex:", ex))
                .onItem().transform(result -> result.size() == 0 ? null : result.iterator().next())
                .onItem().invoke(snapshot -> logger.infof("(getSnapshot) snapshot version: %s",
                        Optional.ofNullable(snapshot).map(Snapshot::getVersion)));
    }

    private <T extends AggregateRoot> T getSnapshotFromClass(Snapshot snapshot, String aggregateId, Class<T> aggregateType) {
        if(snapshot == null) {
            // create new snapshot from new aggregate
            final var defaultSnapshot = snapshotFromAggregate(getAggregate(aggregateId, aggregateType));
            return aggregateFromSnapshot(defaultSnapshot, aggregateType);
        }
        // create aggregate from existing snapshot
        return aggregateFromSnapshot(snapshot, aggregateType);
    }

    private <T extends AggregateRoot> Uni<RowSet<Row>> saveSnapshot(SqlConnection client, T aggregate) {
        aggregate.toSnapshot();
        final var snapshot = snapshotFromAggregate(aggregate);
        return client.preparedQuery(SAVE_SNAPSHOT_QUERY).execute(Tuple.of(
                        snapshot.getAggregateId(),
                        snapshot.getAggregateType(),
                        Objects.isNull(snapshot.getData()) ? new byte[]{} : snapshot.getData(),
                        Objects.isNull(snapshot.getMetaData()) ? new byte[]{} : snapshot.getMetaData(),
                        snapshot.getVersion()))
                .onFailure().invoke(ex -> logger.error("(saveSnapshot) preparedQuery execute:", ex));
    }

    private Uni<RowSet<Row>> handleConcurrency(SqlConnection client, String aggregateId) {
        return client.preparedQuery(HANDLE_CONCURRENCY_QUERY).execute(Tuple.of(aggregateId))
                .onFailure().invoke(ex -> logger.error("(handleConcurrency) ex", ex));
    }

    private <T extends AggregateRoot> T getAggregate(final String aggregateId, final Class<T> aggregateType) {
        try {
            return aggregateType.getConstructor(String.class).newInstance(aggregateId);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends AggregateRoot> Uni<T> raiseAggregateEvents(T aggregate, RowSet<Event> events) {
        if(events != null && events.rowCount() > 0) {
            events.forEach(event -> {
                aggregate.raiseEvent(event);
                logger.infof("(raiseAggregateEvent) event version: %s", event.getVersion());
            });
            return Uni.createFrom().item(aggregate);
        }
        else {
            return (aggregate.getVersion() == 0) ? Uni.createFrom().failure(new BankAccountNotFoundException(aggregate.getId())) : Uni.createFrom().item(aggregate);
        }
    }

    private static Event eventFromRow(Row row) {
        return Event.builder()
                .id(row.getUUID(EVENT_ID))
                .aggregateId(row.getString(AGGREGATE_ID))
                .aggregateType(row.getString(AGGREGATE_TYPE))
                .eventType(row.getString(EVENT_TYPE))
                .data(row.getBuffer(DATA).getBytes())
                .metaData(row.getBuffer(METADATA).getBytes())
                .version(row.getLong(VERSION))
                .timeStamp(row.getOffsetDateTime(TIMESTAMP).toZonedDateTime())
                .build();
    }

    private static Snapshot snapshotFromRow(Row row) {
        return Snapshot.builder()
                .id(row.getUUID(SNAPSHOT_ID))
                .aggregateId(row.getString(AGGREGATE_ID))
                .aggregateType(row.getString(AGGREGATE_TYPE))
                .data(row.getBuffer(DATA).getBytes())
                .metaData(row.getBuffer(METADATA).getBytes())
                .version(row.getLong(VERSION))
                .timeStamp(row.getLocalDateTime(TIMESTAMP))
                .build();
    }

    private static <T extends AggregateRoot> Snapshot snapshotFromAggregate(final T aggregate) {
        byte[] bytes = SerializerUtils.serializeToJsonBytes(aggregate);
        return Snapshot.builder()
                .id(UUID.randomUUID())
                .aggregateId(aggregate.getId())
                .aggregateType(aggregate.getType())
                .version(aggregate.getVersion())
                .data(bytes)
                .timeStamp(LocalDateTime.now())
                .build();
    }

    public static <T extends AggregateRoot> T aggregateFromSnapshot(final Snapshot snapshot, final Class<T> valueType) {
        return SerializerUtils.deserializeFromJsonBytes(snapshot.getData(), valueType);
    }
}
