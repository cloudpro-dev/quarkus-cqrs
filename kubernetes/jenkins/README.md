# Jenkins on Kubernetes

Deploy Jenkins on Kubernetes using the (Jenkins Kubernetes Operator)[https://jenkinsci.github.io/kubernetes-operator/]

```shell
kubectl apply -f https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/config/crd/bases/jenkins.io_jenkins.yaml
```

Define the Jenkins server using the Jenkins operator API:
```shell
kubectl create ns jenkins-operator
kubectl create ns jenkins
```

Install the required resources in `jenkins-operator` namespace with:
```shell
kubectl apply -n jenkins-operator -f jenkins-operator-rbac.yml
```

Install the Operator in `jenkins-operator` namespace with:
```shell
kubectl apply -n jenkins-operator -f jenkins-operator.yml
```

Below you can find manifest with RBAC that needs to be created in `jenkins` namespace:
```shell
kubectl apply -n jenkins -f jenkins-ns-rbac.yml
```

Now deploy the Jenkins instance:
```shell
kubectl apply -n jenkins -f jenkins-instance.yml
```