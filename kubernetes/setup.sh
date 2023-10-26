# Namespace
kubectl create namespace cqrs && \

# Infrastructure
kubectl apply -f ./postgres.yml -n cqrs && \
kubectl apply -f ./mongo.yml -n cqrs && \
kubectl apply -f ./kafka.yml -n cqrs