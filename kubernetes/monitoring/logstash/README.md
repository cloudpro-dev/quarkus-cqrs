# Logstash on Kubernetes

Setup Logstash in Kubernetes in a single command:
```shell
kubectl apply -f logstash-configmap.yml -n monitoring && \
kubectl apply -f logstash-deployment.yml -n monitoring && \
kubectl apply -f logstash-service.yml -n monitoring
``` 

Remove Logstash from Kubernetes
```shell
kubectl delete -f logstash-configmap.yml -n monitoring  && \
kubectl delete -f logstash-deployment.yml -n monitoring && \
kubectl delete -f logstash-service.yml -n monitoring
```