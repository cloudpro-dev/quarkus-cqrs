kubectl delete -f https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/config/crd/bases/jenkins.io_jenkins.yaml && \

# Remove the Jenkins operator
kubectl delete -n jenkins -f https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/deploy/all-in-one-v1alpha2.yaml && \

# Remove the namespace
kubectl delete ns jenkins