# Infrastructure
kubectl delete -f ./postgres.yml -n cqrs && \
kubectl delete -f ./mongo.yml -n cqrs && \
kubectl delete -f ./kafka.yml -n cqrs && \

kubectl delete namespace cqrs