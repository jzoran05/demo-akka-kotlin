/*
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.cluster.bootstrap.demo

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ClusterEvent
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.http.javadsl.server.AllDirectives
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer

class ClusterApp internal constructor() : AllDirectives() {

    init {
        val system = ActorSystem.create()

        val http = Http.get(system)
        val materializer = ActorMaterializer.create(system)
        val cluster = Cluster.get(system)

        system.log().info("Started [" + system + "], cluster.selfAddress = " + cluster.selfAddress() + ")")


        system.log().info("Starting AkkManagement...")
        AkkaManagement.get(system).start()
        system.log().info("Started AkkManagement...")

        ClusterBootstrap.get(system).start()
        system.log().info("Started ClusterBootstrap...")

        cluster.subscribe(system.actorOf(Props.create(ClusterWatcher::class.java)),
                ClusterEvent.initialStateAsEvents(),
                ClusterEvent.ClusterDomainEvent::class.java)
        system.log().info("Cluster subscribe completed...")

        // create actors
        val noisyActor = createNoisyActor(system)
        system.log().info("Noisy Actor created...")

        val routeHelper = RouteHelper()
        val routeFlow = routeHelper.createRoutes(system, cluster).flow(system, materializer)
        system.log().info("Route Created...")

        val binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("0.0.0.0", 8080), materializer)
        system.log().info("Binding Created...")

        cluster.registerOnMemberUp({
            system.log().info("Cluster member is up!")
            noisyActor.tell(PoisonPill.getInstance(), ActorRef.noSender())
        })
        system.log().info("Cluster Registered...")

    }

    private fun createNoisyActor(system: ActorSystem): ActorRef {
        return system.actorOf(NoisyActor.props(), "NoisyActor")
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            ClusterApp()
        }
    }
}

