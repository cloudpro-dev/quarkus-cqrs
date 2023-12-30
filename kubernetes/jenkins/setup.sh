#!/bin/bash

# Install Jenkins operator
kubectl apply -f https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/config/crd/bases/jenkins.io_jenkins.yaml && \

# Create namespace
kubectl create ns jenkins && \

# Create the operator instance
kubectl apply -n jenkins -f https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/deploy/all-in-one-v1alpha2.yaml && \

# Create a Jenkins instance
kubectl apply -n jenkins -f ./jenkins-instance.yml