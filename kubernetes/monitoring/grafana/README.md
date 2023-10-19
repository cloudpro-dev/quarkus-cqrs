# Grafana on Kubernetes

To deploy a working instance of Grafana use the following command:
```shell
kubectl apply -f grafana-configmap.yml -n monitoring && \
kubectl apply -f grafana-dashboards.yml -n monitoring && \
kubectl apply -f grafana-deployment.yml -n monitoring && \
kubectl apply -f grafana-service.yml -n monitoring
```

Remove Grafana from Kubernetes
```shell
kubectl delete -f grafana-configmap.yml -n monitoring && \
kubectl delete -f grafana-dashboards.yml -n monitoring && \
kubectl delete -f grafana-deployment.yml -n monitoring && \
kubectl delete -f grafana-service.yml -n monitoring
```
