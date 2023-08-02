# Filebeat on Kubernetes

To help aggregate the logs of all the containers in the Kubernetes cluster, Filebeat is configured to export all STDOUT
and STDERR to Logstash for further processing.

```shell
kubectl apply -f rbac.yml -n monitoring
```

```shell
kubectl apply -f filebeat-configmap.yml -n monitoring
```

```shell
kubectl apply -f filebeat-daemon.yml -n monitoring
```

```shell
kubectl delete -f filebeat-configmap.yml -n monitoring && \
kubectl delete -f filebeat-daemon.yml -n monitoring
```