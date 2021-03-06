# Akka Kotlin Samples

0) Deploy as a Cluster (from IDE)
1) Deploy as a single Docker container
2) Use Akka Management (Cluster) HTTP 
3) Deploy as a Cluster (using multiple Docker containers)
4) Use Cluster Bootstrap - Local Config
5) Use Cluster Bootstrap - Kubernetes

## 0) Deploy as a Cluster (from IDE) - SimpleClusterApp.kt

### 1.1) Deploy from IDE

Note: Use kotlin.bat xx.jar to start the jar

Open application.conf

To enable cluster capabilities in your Akka project you should, at a minimum, add the remote settings, and use cluster for akka.actor.provider. The akka.cluster.seed-nodes should normally also be added to your application.conf file.
The seed nodes are configured contact points which newly started nodes will try to connect with in order to join the cluster.
Note that if you are going to start the nodes on different machines you need to specify the ip-addresses or host names of the machines in application.conf instead of 127.0.0.1.
Open SimpleClusterApp.java.

The small program together with its configuration starts an ActorSystem with the Cluster enabled. It joins the cluster and starts an actor that logs some membership events. Take a look at the SimpleClusterListener.java actor.
You can read more about the cluster concepts in the documentation.
To run this sample, go to the Run tab, and start the application main class sample.cluster.simple.SimpleClusterApp if it is not already started.
SimpleClusterApp starts three actor systems (cluster members) in the same JVM process. 


### 1.2) Deploy from command line
It can be more interesting to run them in separate processes. Stop the application in the Run tab and then open three terminal windows.
In the first terminal window, start the first seed node with the following command (on one line):


<path to activator dir>/activator 
  "runMain sample.cluster.simple.SimpleClusterApp 2551"		
2551 corresponds to the port of the first seed-nodes element in the configuration. In the log output you see that the cluster node has been started and changed status to 'Up'.
In the second terminal window, start the second seed node with the following command (on one line):


<path to activator dir>/activator 
  "runMain sample.cluster.simple.SimpleClusterApp 2552"		
2552 corresponds to the port of the second seed-nodes element in the configuration. In the log output you see that the cluster node has been started and joins the other seed node and becomes a member of the cluster. Its status changed to 'Up'.
Switch over to the first terminal window and see in the log output that the member joined.
Start another node in the third terminal window with the following command (on one line):


<path to activator dir>/activator 
  "runMain sample.cluster.simple.SimpleClusterApp 0"		
Now you don't need to specify the port number, 0 means that it will use a random available port. It joins one of the configured seed nodes. Look at the log output in the different terminal windows.
Start even more nodes in the same way, if you like.
Shut down one of the nodes by pressing 'ctrl-c' in one of the terminal windows. The other nodes will detect the failure after a while, which you can see in the log output in the other terminals.
Look at the source code of the actor again. It registers itself as subscriber of certain cluster events. It gets notified with an snapshot event, CurrentClusterState that holds full state information of the cluster. After that it receives events for changes that happen in the cluster.


