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

# Testing the Native image

To test the native image, run the integration tests against the generated binary:
```shell
./mvnw verify -Pnative
```

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

### Get bank account by ID
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

# Native Image
## Building
Docker containers are generated using the native binary because of the `quarkus-container-image-jib` dependency and by adding the following properties to the application:
- `quarkus.container-image.build`
- `quarkus.native.container-build`
_Note:_ If you want the container to use a non-native image, then set the `quarkus.native.container-build` to `false`.
```shell
mvn package -DskipTests -Pnative
```

## Testing
Integration tests are used to make sure that the application functions correct once it has been converted to a native application.
```shell
./mvnw clean verify -Pnative
```

# Production mode
To run in `prod` profile from your IDE, you will need to add a VM Options for `-Dquarkus.profile=prod`


# Kafka Streams
Processing of the event emitted from the `event-store` Kafka topic are aggregated by `event-streams` into a new Kafka 
topic `event-store-aggregated` which is then consumed by other applications.


