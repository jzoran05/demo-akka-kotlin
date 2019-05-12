package akka.cluster.bootstrap.demo;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.MemberStatus;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

import java.util.HashSet;
import java.util.Set;
//import org.jboss.netty.util.internal.DeadLockProofWorker.start;

public class RouteHelper extends AllDirectives {

    public Route createRoutes(ActorSystem system, Cluster cluster) {
        Set<MemberStatus> readyStates = new HashSet<MemberStatus>();
        readyStates.add(MemberStatus.up());
        readyStates.add(MemberStatus.weaklyUp());

        return
                // only handle GET requests
                get(() -> route(
                        path("ready", () -> {
                                    MemberStatus selfState = cluster.selfMember().status();
                                    system.log().debug("ready? clusterState:" + selfState);
                                    if (readyStates.contains(cluster.selfMember().status()))
                                        return complete(StatusCodes.OK);
                                    else
                                        return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                                }
                        ),
                        path("alive", () ->
                                // When Akka HTTP can respond to requests, that is sufficient
                                // to consider ourselves 'live': we don't want K8s to kill us even
                                // when we're in the process of shutting down (only stop sending
                                // us traffic, which is done due to the readyness check then failing)
                                complete(StatusCodes.OK)
                        ),
                        path("hello", () ->
                                complete("<h1>Say hello to akka-http</h1>"))
                ));
    }
}
