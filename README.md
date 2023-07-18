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



# Docker deployment

To run the application using a local Docker setup instead of Development mode, you can run the `docker-compose.yml` file in the project root directory.

This will package the application as a Docker image and then run the whole stack:
```shell
mvn clean package \
  -DskipTests \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.group=cqrs
  
docker-compose -f docker-compose.yml -f docker-compose-dev.yml up -d
```

If you wish to deploy only the infrastructure on Docker and then connect with local instance of the application for debugging.
```shell
mvn clean package \
  -DskipTests \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.group=cqrs \
  -Dquarkus.container-image.group=cqrs

      - QUARKUS_HTTP_PORT=9020
      - QUARKUS_HTTP_SSL_PORT=9021
      - QUARKUS_DATASOURCE_USERNAME=postgres
      - QUARKUS_DATASOURCE_PASSWORD=postgres
      - KAFKA_BOOTSTRAP_SERVERS=BROKER://localhost:9092
      - QUARKUS_DATASOURCE_DB_KIND=postgresql
      - QUARKUS_DATASOURCE_REACTIVE_URL=vertx-reactive:postgresql://localhost:5432/microservices
      - QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/microservices
```

# Production mode
To run in `prod` profile from your IDE, you will need to add a VM Options for `-Dquarkus.profile=prod`

# Deploying the application in Kubernetes

```shell
kubectl create ns cqrs
```

```shell
kubectl apply -f ./kubernetes/postgres.yml -n cqrs
kubectl apply -f ./kubernetes/mongo.yml -n cqrs
kubectl apply -f ./kubernetes/kafka.yml -n cqrs
```

Kafka client can be run to perform activities on the Kafka instances
```shell
kubectl run kafka-client -n cqrs --rm -ti --image bitnami/kafka:3.1.0 -- bash

ls /opt/bitnami/kafka/bin
kafka-acls.sh
kafka-broker-api-versions.sh
kafka-cluster.sh
kafka-configs.sh
kafka-console-consumer.sh
kafka-console-producer.sh
kafka-consumer-groups.sh
kafka-consumer-perf-test.sh
kafka-delegation-tokens.sh
kafka-delete-records.sh
```

**Important:** We must build the image via the Minikube Docker Daemon to make it available to the Kubernetes cluster for deployment:
```
eval $(minikube -p minikube docker-env)
```

### Standard JAR build

```
./mvnw clean package \
    -DskipTests=true \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.group=cqrs \
    -Dquarkus.kubernetes.deploy=true
```

### Native image build
```
./mvnw clean package \
    -DskipTests=true \
    -Dnative \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.group=cqrs \
    -Dquarkus.native.remote-container-build=true \
    -Dquarkus.kubernetes.deploy=true
```

# Password encryption

Kubernetes stores the content of all secrets in a base 64 encoded format. If you want to see how your string will appear in a base64 format, execute the following.
```shell
echo "devopscube" | base64 
//after encoding it, this becomes ZGV2b3BzY3ViZQo=
```

If you want to decode a base64 string. Run
```shell
echo "ZGV2b3BzY3ViZQo=" | base64 --decode
//after decoding it, this will give devopscube
```

# Application

## Testing

# 
To test the standard JAR version of the application you can run the standard integration tests:
```shell
./mvnw clean verify
```

# Native image testing
Integration tests are used to make sure that the application functions correct once it has been converted to a native application.
```shell
./mvnw clean verify -Pnative
```

## Commands
You can manually call the application API to create a new account and perform some actions.

### Create a new bank account
```shell
EVENT_STORE_URL=$(minikube service --url --https event-store -n cqrs | head -n 1)
VIEW_STORE_URL=$(minikube service --url view-store -n cqrs | head -n 1)
ID=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"test@test.com", "userName":"testuser12345", "address":"11 Test Lane, Test Town, TT1 1TT"}' $EVENT_STORE_URL/api/v1/bank) 
echo "$ID"
```

### Update email address
```shell
curl -v -X POST -H "Content-Type: application/json" -d '{"email":"test123@test.com"}' $EVENT_STORE_URL/api/v1/bank/email/$ID
```

### Update address
```shell
curl -v -X POST -H "Content-Type: application/json" -d '{"address":"64 Test Ave, Testington, 1TT TT1"}' $EVENT_STORE_URL/api/v1/bank/address/$ID
```

### Deposit funds
```shell
curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 500.00}' $EVENT_STORE_URL/api/v1/bank/deposit/$ID
```

### Withdraw funds
```shell
curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 100.00}' $EVENT_STORE_URL/api/v1/bank/withdraw/$ID
```

## Queries

### Find all sorted by balance
```shell
curl -v "$VIEW_STORE_URL/api/v1/bank/balance?page=0&size=3"
```

### Get bank account by ID
```shell
curl -v "$VIEW_STORE_URL/api/v1/bank/$ID"
```