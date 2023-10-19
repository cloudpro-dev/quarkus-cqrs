# MinIO on Kubernetes

Note: The job will create a bucket named `loki` for Loki log storage and set access to `public`.

```shell
kubectl apply -f minio-deployment.yml -n monitoring && \
kubectl apply -f minio-service.yml -n monitoring
kubectl apply -f minio-job.yml -n monitoring
```

```shell
kubectl delete -f minio-deployment.yml -n monitoring && \
kubectl delete -f minio-service.yml -n monitoring
kubectl delete -f minio-job.yml -n monitoring
```