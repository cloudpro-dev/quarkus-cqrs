# Namespace
kubectl create namespace monitoring && \

# MinIO
kubectl apply -f ./minio/minio-deployment.yml -n monitoring && \
kubectl apply -f ./minio/minio-service.yml -n monitoring && \

# Loki
kubectl apply -f ./loki/loki-configmap.yml -n monitoring && \
kubectl apply -f ./loki/loki-deployment.yml -n monitoring && \
kubectl apply -f ./loki/loki-service.yml -n monitoring && \

# Logstash
kubectl apply -f ./logstash/logstash-configmap.yml -n monitoring && \
kubectl apply -f ./logstash/logstash-deployment.yml -n monitoring && \
kubectl apply -f ./logstash/logstash-service.yml -n monitoring && \

# Prometheus
kubectl apply -f ./prometheus/rbac.yml && \
kubectl apply -f ./prometheus/prometheus-configmap.yml -n monitoring && \
kubectl apply -f ./prometheus/prometheus-deployment.yml -n monitoring && \
kubectl apply -f ./prometheus/prometheus-service.yml -n monitoring && \

# Tempo
kubectl apply -f ./tempo/tempo-configmap.yml -n monitoring && \
kubectl apply -f ./tempo/tempo-deployment.yml -n monitoring && \
kubectl apply -f ./tempo/tempo-service.yml -n monitoring && \

# Grafana
kubectl apply -f ./grafana/grafana-configmap.yml -n monitoring && \
kubectl apply -f ./grafana/grafana-dashboards.yml -n monitoring && \
kubectl apply -f ./grafana/grafana-deployment.yml -n monitoring && \
kubectl apply -f ./grafana/grafana-service.yml -n monitoring



