# Logstash on Kubernetes

Setup Logstash in Kubernetes in a single command:
```shell
kubectl apply -f logstash-configmap.yml && \
kubectl apply -f logstash-deployment.yml && \
kubectl apply -f logstash-service.yml
``` 

Remove Logstash from Kubernetes
```shell
kubectl delete -f logstash-configmap.yml && \
kubectl delete -f logstash-deployment.yml && \
kubectl delete -f logstash-service.yml
```