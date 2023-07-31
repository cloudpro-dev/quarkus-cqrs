# event-sourcing

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
mvn compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
mvn package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
mvn package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
mvn package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script 
mvn package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/event-sourcing-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

# Testing the Native image

To test the native image, run the integration tests against the generated binary:
```shell
mvn verify -Pnative
```

## Related Guides

- SmallRye Fault Tolerance ([guide](https://quarkus.io/guides/microprofile-fault-tolerance)): Build fault-tolerant network services
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, JPA)
- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A JAX-RS implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- SmallRye Reactive Messaging - Kafka Connector ([guide](https://quarkus.io/guides/kafka-reactive-getting-started)): Connect to Kafka with Reactive Messaging
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC
- Reactive PostgreSQL client ([guide](https://quarkus.io/guides/reactive-sql-clients)): Connect to the PostgreSQL database using the reactive pattern



# Docker deployment

## Pre-requisites
You should have a recent version of Docker installed (19.03.0+)

Deploy the infrastructure to Docker:
```shell
docker-compose -f docker-compose.yml up -d
```

Next you need to build the Docker container images from the applications:
```shell
mvn clean package \
  -DskipTests \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.group=cqrs
```

If you wish to build a native version of the application into the container image, then use the following command:
```shell
mvn clean package \
  -Pnative \
  -DskipTests \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.group=cqrs
```

Finally, run the packaged application containers as a single platform:
```shell
docker-compose -f docker-compose-dev.yml up -d
```

# Kubernetes deployment

## Pre-requisites
If you have not already, start Minikube and wait for it to become ready.
```shell
minikube start
minikube addons enable metrics-server
```

Before we can deploy the applications we must set up the necessary infrastructure in the Kubernetes cluster.

Create a new namespace for the whole platform:
```shell
kubectl create ns cqrs
```

Deploy the infrastructure to Kubernetes:
```shell
kubectl apply -f ./kubernetes/postgres.yml -n cqrs
kubectl apply -f ./kubernetes/mongo.yml -n cqrs
kubectl apply -f ./kubernetes/kafka.yml -n cqrs
```

Next we need to create the Kafka topics with the correct partition configuration.

To access the Kafka server in the Kubernetes cluster, we must add a new Pod running Kafka client tools:
```shell
kubectl run kafka-client -n cqrs --rm -ti --image bitnami/kafka:3.1.0 -- bash
```

Once the new pod terminal is available, run the following commands:
```shell
/opt/bitnami/kafka/bin/kafka-topics.sh \
  --bootstrap-server=BROKER://kafka-svc.cqrs.svc.cluster.local:9092 \ 
  --topic event-store \
  --partitions=3 \
  --create
```

Finally, we query the topic to make sure the configuration is correct:
```shell
/opt/bitnami/kafka/bin/kafka-topics.sh \
  --bootstrap-server=BROKER://kafka-svc.cqrs.svc.cluster.local:9092 \ 
  --topic event-store \ 
  --describe

Topic: event-store      TopicId: jOA78GwxQM-WIlg-8aGNJA PartitionCount: 3       ReplicationFactor: 1    Configs: min.insync.replicas=1,segment.bytes=1073741824
        Topic: event-store      Partition: 0    Leader: 0       Replicas: 0     Isr: 0
        Topic: event-store      Partition: 1    Leader: 0       Replicas: 0     Isr: 0
        Topic: event-store      Partition: 2    Leader: 0       Replicas: 0     Isr: 0
```
Note: All other topics will be created on demand by the application with a single partition.

If you wish to inspect the messages on the Kafka topic/partition, you can use:
```shell
/opt/bitnami/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server=BROKER://kafka-svc.cqrs.svc.cluster.local:9092 \
  --topic event-store \
  --from-beginning \
  --partition 0
```

## Deploying the applications

The next step is to package the application into containers which can then be deployed directly into the Kubernetes cluster.

**Important:** We must build the image via the Minikube Docker Daemon to make it available to the Kubernetes cluster for deployment:
```
eval $(minikube -p minikube docker-env)
```

### Standard JAR build
To build a standard Java JAR deployment, use the following command:
```
mvn clean package \
    -DskipTests=true \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.group=cqrs \
    -Dquarkus.kubernetes.namespace=cqrs \
    -Dquarkus.kubernetes.deploy=true
```

### Native image build
To build a native image version of the application, use the following commands:
```
mvn clean package \
    -Pnative \
    -DskipTests=true \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.group=cqrs \
    -Dquarkus.native.remote-container-build=true \
    -Dquarkus.kubernetes.namespace=cqrs \
    -Dquarkus.kubernetes.deploy=true
```

## Scale the consumers
Once all the consumers come up, we can scale each of the services horizontally using the following command:
```shell
kubectl scale -n cqrs deployment view-store --replicas=3
```

## Testing

### Standard JAR testing 
To test the standard JAR version of the application you can run the standard integration tests:
```shell
mvn clean verify
```

### Native image testing
Integration tests are used to make sure that the application functions correct once it has been converted to a native application.
```shell
mvn clean verify -Pnative
```

Note: If you receive an error similar to the following:
```shell
Error: Invalid Path entry event-store-1.0.0-SNAPSHOT-runner.jar
Caused by: java.nio.file.NoSuchFileException: /project/event-store-1.0.0-SNAPSHOT-runner.jar
```
Then check you are not connected to the Minikube docker instance! An easy way to check is by running `docker ps` and
checking the output for `k8.io` based containers. You can always start a new terminal to make sure.

## Manual testing
You can manually call the application API to create a new account and perform some actions.

To run the commands against the local instances, create the following env vars:
```shell
export EVENT_STORE_URL=localhost:9020
export VIEW_STORE_URL=localhost:9010
export AGGREGATE_VIEW_URL=localhost:9040
```

To run the commands against a Kubernetes deployment, create the following env vars:
```shell
export EVENT_STORE_URL=$(minikube service --url event-store -n cqrs | head -n 1)
export VIEW_STORE_URL=$(minikube service --url view-store -n cqrs | head -n 1)
export AGGREGATE_VIEW_URL=$(minikube service --url aggregate-view -n cqrs | head -n 1)
```

Now you can run the commands script which will execute all the bank account commands and then query back the latest state of the account from the query side.
```shell
./commands.sh
```

# Monitoring

Grafana UI
http://localhost:3005/

Prometheus UI
http://localhost:9090/

Loki
http://localhost:3100/

# Open Telemetry Collector

To show the metrics which are collected by Prometheus from OTel Collector:
```shell
curl http://localhost:8889/metrics
```

# Tempo

## Configuration
```shell
curl localhost:3200/status/config | grep metrics
```

## Metrics
Tempo will show metrics which indicate the state of the `metrics-generator`:
```shell
curl http://localhost:3200/metrics | grep tempo_metrics_generator
```

## Trace by ID
```shell
curl http://localhost:3200/api/traces/154634d2162353cb2d0ed94ab1bde6f0
```

Tempo [span metrics processor](https://grafana.com/docs/tempo/latest/metrics-generator/span_metrics/) exports the following metrics to the configured Prometheus instance:
- `traces_spanmetrics_latency` - Duration of the span (Histogram)
- `traces_spanmetrics_calls_total` - Total count of the span (Counter)
- `traces_spanmetrics_size_total` - Total size of spans ingested (Counter)

Tempo [service graphs](https://grafana.com/docs/tempo/latest/metrics-generator/service_graphs/#service-graphs) exports the following metrics to the configured Prometheus instance:
- `traces_service_graph_request_total` - Total count of requests between two nodes (Counter)
- `traces_service_graph_request_failed_total` - Total count of failed requests between two nodes (Counter)
- `traces_service_graph_request_server_seconds` - Time for a request between two nodes as seen from the server (Histogram)
- `traces_service_graph_request_client_seconds` - Time for a request between two nodes as seen from the client (Histogram)
- `traces_service_graph_unpaired_spans_total` - Total count of unpaired spans (Counter)
- `traces_service_graph_dropped_spans_total` - Total count of dropped spans (Counter)

Query Prometheus for the relevant metrics:
```shell
curl 'http://localhost:9090/api/v1/query?query=traces_spanmetrics_latency_bucket'
{"status":"success","data":{"resultType":"vector","result":[]}}
```

# Logstash

```shell
curl -XGET 'localhost:9600/?pretty'
curl -XGET 'localhost:9600/_node/pipelines?pretty'
```

You can test that the Logstash server is receiving data by using the `nc` command:
```shell
echo '{"message": {"someField":"someValue"} }' > tmp.json
nc localhost:5400 < tmp.json
```

# Loki

Logs received by Logstash will be exported to Loki for ingestion and storage.

To query the logs in Loki:
```shell
curl -G -s  "http://localhost:3100/loki/api/v1/query_range" --data-urlencode 'query={traceId="978fab8a10b12fffec43e78b43e44936"}' | jq
```

# Exemplars

Quarkus supports Exemplars (metrics with an associated `traceId` and `spanId`) via Prometheus using the standard `quarkus-micrometer-prometheus` extension.

By adding the `@Timed` annotation to your methods, you will see that Prometheus metrics will contain the extra information.
```shell
curl -v http://localhost:9010/q/metrics | grep hello
view_store_message_process_seconds_bucket{class="com.example.BankAccountResource",exception="none",method="getAllByBalance",le="0.016777216"} 2.0 # {span_id="d1f2591c877b95b9",trace_id="08c32eb410514f827454363c63fc1ebb"} 0.016702041 1684067985.642
```
_Note: Not every entry will have traceId and spanId as Exemplars are sampled data, not all the data._