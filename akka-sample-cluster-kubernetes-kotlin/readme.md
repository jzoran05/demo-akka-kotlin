# akka-sample-cluster-kubernetes-java

This is an example Maven project showing how to create an Akka Cluster on Kubernetes.

It is not always necessary to use Akka Cluster when deploying an Akka application to Kubernetes: if your application can be designed as independent stateless services that do not need coordination, deploying them on Kubernetes as individual Akka application without Akka Cluster can be a good fit. When coordination between nodes is necessary, this is where the Akka Cluster features become interesting and it is worth consider making the nodes form an Akka Cluster.

Starting
First, package the application and make it available locally as a docker image:

```bash
mvn clean package docker:build
```

Then akka-cluster.yml should be sufficient to deploy a 2-node Akka Cluster:

```bash
kubectl apply -f kubernetes/akka-cluster.yml
```

How it works
This example uses Akka Cluster Bootstrap to initialize the cluster, using the Kubernetes API discovery mechanism to find peer nodes.