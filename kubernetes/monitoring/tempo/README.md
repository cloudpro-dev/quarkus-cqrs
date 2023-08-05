# Tempo on Kubernetes

```shell
kubectl apply -f tempo-configmap.yml -n monitoring && \
kubectl apply -f tempo-deployment.yml -n monitoring && \
kubectl apply -f tempo-service.yml -n monitoring
```

```shell
kubectl delete -f tempo-configmap.yml -n monitoring && \
kubectl delete -f tempo-deployment.yml -n monitoring && \
kubectl delete -f tempo-service.yml -n monitoring
```

