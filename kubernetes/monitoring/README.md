# Kubernetes Monitoring Stack
The monitoring stack is used to receive logs and telemetry from the infrastructure and the applications.

First create a new namespace to hold the monitoring stack:
```shell
kubectl create namespace monitoring
```

Services should be deployed in the following order:
1. MinIO
2. Loki
3. Logstash
4. Prometheus
5. Tempo
6. Grafana

See the individual directories for information on how to deploy each of these services.