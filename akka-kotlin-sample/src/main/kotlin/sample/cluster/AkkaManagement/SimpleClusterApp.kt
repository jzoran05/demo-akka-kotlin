package sample.cluster.AkkaManagement

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ClusterEvent
import akka.cluster.bootstrap.demo.RouteHelper
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.AkkaManagement
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import sample.cluster.actors.SimpleClusterListener

// TODO: Add Cluster Bootstap - use local config (skip for now - sample code exists on Akka local config page)
// TODO: Add Cluster Bootstrap - use Akka discovery - Akka DNS
// TODO: Add Cluster Bootstrap - use Akka discovery - Kubernetes API
object SimpleClusterApp {

    private val appClusterSystemName: String = "ClusterSystem"
    /*
    args:
    1) clusterType:
    - local,
    - bootstrapLocalConfig,
    - bootstrapAkkaDNS (either JVM or Kubernetes by using Akka DNS discovery),
    - bootstrapKubernetesAPI (cluster bootstrap by using Akka discovery with Kubernetes API)
    2)
    */
    @JvmStatic
    fun main(args: Array<String>) {

        // if app is not invoked with a port parameter, then create three instances with predefined ports
        when(args.size) {
            0 -> startupLocalCluster(arrayOf("2551", "2552", "2553")) // start without specified ports and provide default
            else -> startSelector(args)
        }

    }

    private fun startSelector(args: Array<String>) {
        when(args[0]) {
            "local" -> {
                if(args.size == 4) startupLocalCluster(args.copyOfRange(1,3)) // e.g. "local" "2551" "2552" "2553"
                else if(args.size == 2) startupLocalCluster(args.copyOfRange(1,1)) // e.g. "local" "2551"
            }
            "bootstrapLocalConfig" -> startupBootstrapLocalConfig(args.copyOfRange(1,3))
            "bootstrapAkkaDNS" -> startupBootstrapAkkaDNS()
            else -> {
                println("Unsupported parameter (0) for cluster type... Exiting...")
                return
            }
        }
    }


    /*
    Starts local cluster either from IDE and predefined ports and single JVM or from terminal by providing port numbers
    and starting separate JVMs
    Note:
    - Uses default application.conf file
    - Exposes Akka Management HTTP port (actor system port - 10)
    */
    //@JvmStatic
    private fun startupLocalCluster(ports: Array<String>) {
        for (port in ports) {

            // Override the configuration of the port
            // To use artery instead of netty, change to "akka.remote.artery.canonical.port"
            // See https://doc.akka.io/docs/akka/current/remoting-artery.html for details
            val managementPort = port.toInt() + 10

            val config = ConfigFactory.parseString(
                    "akka.remote.netty.tcp.port=$port")
                    .withFallback(ConfigFactory.parseString("akka.management.http.port = $managementPort"))
                    .withFallback(ConfigFactory.load("application1"))

            // Create an Akka system
            val system = ActorSystem.create(appClusterSystemName, config)

            val cluster = Cluster.get(system)
            system.log().info("Started [" + system + "], cluster.selfAddress = " + cluster.selfAddress() + ")")

            //#start-akka-management
            AkkaManagement.get(system).start()

            // Create an actor that handles cluster domain events
            system.actorOf(Props.create(SimpleClusterListener::class.java), "clusterListener")

            cluster.registerOnMemberUp { system.log().info("Cluster member is up!") }
        }
    }

    //@JvmStatic
    private fun startupBootstrapLocalConfig(ports: Array<String>) {
        for (port in ports) {
            // Override the configuration of the port
            // To use artery instead of netty, change to "akka.remote.artery.canonical.port"
            // See https://doc.akka.io/docs/akka/current/remoting-artery.html for details
            val managementPort = port.toInt() + 10

            val config = ConfigFactory.parseString(
                    "akka.remote.netty.tcp.port=$port")
                    .withFallback(ConfigFactory.parseString("akka.management.http.port = $managementPort"))
                    .withFallback(ConfigFactory.load())

            // Create an Akka system
            val system = ActorSystem.create(appClusterSystemName, config)

            val cluster = Cluster.get(system)
            system.log().info("Started [" + system + "], cluster.selfAddress = " + cluster.selfAddress() + ")")

            //#start-akka-management
            AkkaManagement.get(system).start()

            // Create an actor that handles cluster domain events
            system.actorOf(Props.create(SimpleClusterListener::class.java), "clusterListener")

            cluster.registerOnMemberUp { system.log().info("Cluster member is up!") }
        }
    }

    fun startupBootstrapAkkaDNS() {

        val config = ConfigFactory.load("bootstrapDNS")
        //.withFallback(ConfigFactory.parseString("akka.management.http.port = $managementPort"))
        val system = ActorSystem.create(appClusterSystemName, config)
        system.log().info("---startupBootstrapAkkaDNS---")

        val http = Http.get(system)
        val materializer = ActorMaterializer.create(system)

        // create cluster
        val cluster = Cluster.get(system)
        system.log().info("Started [" + system + "], cluster.selfAddress = " + cluster.selfAddress() + ")")

        system.log().info("Starting AkkManagement...")
        AkkaManagement.get(system).start()
        system.log().info("Started AkkManagement...")

        ClusterBootstrap.get(system).start()
        system.log().info("Started ClusterBootstrap...")

        // create actors
        //val noisyActor = system.actorOf(NoisyActor.props(), "NoisyActor")
        //system.log().info("Noisy Actor created...")

        // Create an actor that handles cluster domain events
        system.actorOf(Props.create(SimpleClusterListener::class.java), "clusterListener")

        // SimpleCluster listener has cluster.subscribe registration
        /*
        cluster.subscribe(system.actorOf(Props.create(ClusterWatcher::class.java)),
                ClusterEvent.initialStateAsEvents(),
                ClusterEvent.ClusterDomainEvent::class.java)
        system.log().info("Cluster subscribe completed...")
        */

        // maybe the route with health check is not required since if management http is working...?
        val routeHelper = RouteHelper()
        val routeFlow = routeHelper.createRoutes(system, cluster).flow(system, materializer)
        system.log().info("Route Created...")

        val binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("0.0.0.0", 8080), materializer)
        system.log().info("Binding Created...")

        cluster.registerOnMemberUp({
            system.log().info("Cluster member is up!")
            //noisyActor.tell(PoisonPill.getInstance(), ActorRef.noSender())
        })
        system.log().info("Cluster Registered...")
    }
}
