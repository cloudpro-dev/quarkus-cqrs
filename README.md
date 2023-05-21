# event-sourcing

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/event-sourcing-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- SmallRye Fault Tolerance ([guide](https://quarkus.io/guides/microprofile-fault-tolerance)): Build fault-tolerant network services
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, JPA)
- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A JAX-RS implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- SmallRye Reactive Messaging - Kafka Connector ([guide](https://quarkus.io/guides/kafka-reactive-getting-started)): Connect to Kafka with Reactive Messaging
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC
- Reactive PostgreSQL client ([guide](https://quarkus.io/guides/reactive-sql-clients)): Connect to the PostgreSQL database using the reactive pattern

# Application

## Commands

### Create a new bank account
```shell
ID=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"test@test.com", "userName":"testuser12345", "address":"11 Test Lane, Test Town, TT1 1TT"}' localhost:9020/api/v1/bank) 
echo "$ID"
```

### Update email address
```shell
curl -v -X POST -H "Content-Type: application/json" -d '{"email":"test123@test.com"}' localhost:9020/api/v1/bank/email/$ID
```

### Update address
```shell
curl -v -X POST -H "Content-Type: application/json" -d '{"address":"64 Test Ave, Testington, 1TT TT1"}' localhost:9020/api/v1/bank/address/$ID
```

### Deposit funds
```shell
curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 500.00}' localhost:9020/api/v1/bank/deposit/$ID
```

### Withdraw funds
```shell
curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 100.00}' localhost:9020/api/v1/bank/withdraw/$ID
```

## Queries

### Find all sorted by balance
```shell
curl -v "localhost:9010/api/v1/bank/balance?page=0&size=3"
```

### Get bank account by Id
```shell
curl -v "localhost:9010/api/v1/bank/$ID"
```

# Docker deployment

To run the application using a local Docker setup instead of Development mode, you can run the `docker-compose.yml` file in the project root directory.
```shell
docker-compose -f docker-compose.yml up -d
```
Run the `event-store` application
```shell
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

```shell
docker-compose exec kafka /kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --from-beginning \
    --property print.key=true \
    --topic quarkus-db-server.public.customers
```

# Production mode
To run in `prod` profile from your IDE, you will need to add a VM Options for `-Dquarkus.profile=prod`

# Monitoring

Jaeger UI
http://localhost:16686/

Grafana UI
http://localhost:3005/

Prometheus UI
http://localhost:9090/

Elasticsearch
http://localhost:9200/

# Jaeger Query
To show the metrics which are collected by Prometheus from OTel Collector:
```shell
http://localhost:14269/metrics
```

# Open Telemetry Collector

To show the metrics which are collected by Prometheus from OTel Collector:
```shell
curl http://localhost:8889/metrics
```

# ElasticSearch

To view the indices that have been created on ElasticSearch use the following command:
```shell
curl http://localhost:9200/_cat/indices?v
```
To view all results delivered to ElasticSearch index:
```shell
curl http://localhost:9200/_search | jq 
```
Search for a specific 
```shell
curl -X GET "http://localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'
{
    "query": {
        "query_string" : {
            "query" : "(traceId:6a3a514cf139c5ae1d166a235fb3d46f)"
        }
    }
}'
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

# Exemplars

Quarkus supports Exemplars (metrics with an associated traceId and spanId) via Prometheus using the standard `quarkus-micrometer-prometheus` extension.

By adding the `@Timed` or `@Counter` annotation to your methods, you will see that Prometheus metrics will contain the extra information.
```shell
curl -v http://localhost:9010/q/metrics | grep hello
hello_world_timer_seconds_bucket{class="com.example.BankAccountResource",exception="none",method="getAllByBalance",le="0.016777216"} 2.0 # {span_id="d1f2591c877b95b9",trace_id="08c32eb410514f827454363c63fc1ebb"} 0.016702041 1684067985.642
hello_world_counter_total{class="com.example.BankAccountResource",exception="none",method="getAllByBalance",result="success"} 2.0 # {span_id="d1f2591c877b95b9",trace_id="08c32eb410514f827454363c63fc1ebb"} 1.0 1684067985.638
```
_Note: Not every entry will have traceId and spanId as Exemplars are sampled data, not all the data._
