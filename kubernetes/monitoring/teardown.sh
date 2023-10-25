# MinIO
kubectl delete -f ./minio/minio-deployment.yml -n monitoring && \
kubectl delete -f ./minio/minio-service.yml -n monitoring && \

# Loki
kubectl delete -f ./loki/loki-configmap.yml -n monitoring && \
kubectl delete -f ./loki/loki-deployment.yml -n monitoring && \
kubectl delete -f ./loki/loki-service.yml -n monitoring && \

# Logstash
kubectl delete -f ./logstash/logstash-configmap.yml -n monitoring && \
kubectl delete -f ./logstash/logstash-deployment.yml -n monitoring && \
kubectl delete -f ./logstash/logstash-service.yml -n monitoring && \

# Prometheus
kubectl delete -f ./prometheus/rbac.yml && \
kubectl delete -f ./prometheus/prometheus-configmap.yml -n monitoring && \
kubectl delete -f ./prometheus/prometheus-deployment.yml -n monitoring && \
kubectl delete -f ./prometheus/prometheus-service.yml -n monitoring && \

# Tempo
kubectl delete -f ./tempo/tempo-configmap.yml -n monitoring && \
kubectl delete -f ./tempo/tempo-deployment.yml -n monitoring && \
kubectl delete -f ./tempo/tempo-service.yml -n monitoring && \

# Grafana
kubectl delete -f ./grafana/grafana-configmap.yml -n monitoring && \
kubectl delete -f ./grafana/grafana-dashboards.yml -n monitoring && \
kubectl delete -f ./grafana/grafana-deployment.yml -n monitoring && \
kubectl delete -f ./grafana/grafana-service.yml -n monitoring

kubectl delete namespace monitoring