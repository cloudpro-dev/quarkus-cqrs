# Loki on Kubernetes

```shell
kubectl apply -f loki-configmap.yml -n monitoring && \
kubectl apply -f loki-deployment.yml -n monitoring && \
kubectl apply -f loki-service.yml -n monitoring
```

```shell
kubectl delete -f loki-configmap.yml -n monitoring && \
kubectl delete -f loki-deployment.yml -n monitoring && \
kubectl delete -f loki-service.yml -n monitoring
```