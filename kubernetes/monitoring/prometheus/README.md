# Prometheus on Kubernetes

Create a new Kubernetes namespace for the monitoring stack:
```shell
kubectl create namespace monitoring
```

## Prometheus

### Manual install

First we need to give Prometheus the necessary permissions to access the full cluster using RBAC:
```shell
kubectl create -f rbac.yml
```

First we create a ConfigMap from our properties file with the necessary Prometheus configuration
```shell
kubectl create configmap prometheus-config --from-file prometheus.yml -n monitoring
```

Next, we will deploy Prometheus using the container image from the Docker Hub repository.
```shell
kubectl create -f prometheus-deployment.yml -n monitoring
```

To access the Prometheus web interface, we also need to expose the deployment as a service. We used the NodePort service type:
```shell
kubectl create -f prometheus-service.yml -n monitoring
```

Once the deployment is exposed, you can access the Prometheus web interface. If you are using Minikube, you can find the Prometheus UI URL by running minikube service with the--url flag:
```shell
minikube service prometheus-service --url -n monitoring
http://192.168.99.100:30900
```
Take note of the URL to access the Prometheus UI.

```shell
kubectl delete -f prometheus-deployment.yml -n monitoring && \
kubectl delete -f prometheus-service.yml -n monitoring
```

### Helm install
First we install the Helm chart for Prometheus
```shell
helm install prometheus stable/prometheus --namespace monitoring
Error: failed to download "stable/prometheus" (hint: running `helm repo update` may help)
```

OK, so we need to add the necessary repository to access the necessary charts:
```shell
helm repo add stable https://charts.helm.sh/stable
"stable" has been added to your repositories
```

Grab the latest Helm charts from the repo:
```shell
helm repo update
```

Now we try to install the Prometheus Helm chart again:
```shell
helm install prometheus stable/prometheus --namespace monitoring
```