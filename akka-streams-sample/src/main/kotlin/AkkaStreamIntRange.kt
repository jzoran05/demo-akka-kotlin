package Akka.Stream.Sample

import akka.stream.*
import akka.stream.javadsl.*

import akka.actor.ActorSystem
import akka.util.ByteString
import java.nio.file.Paths
import java.math.BigInteger
import java.time.Duration
import java.util.concurrent.CompletionStage
import akka.stream.IOResult


    fun main(args: Array<String>) {

        // #create-materializer
        val system = ActorSystem.create("QuickStart")
        val materializer = ActorMaterializer.create(system)

        // create source
        val source = Source.range(1, 100)

        // run source
        source.runForeach({ i -> println(i) }, materializer)

        // transform source
        val factorials = source
            .scan(BigInteger.ONE, {
                acc, next -> acc.multiply(BigInteger.valueOf(next.toLong()))
            })

        val result = factorials
            .map { num -> ByteString.fromString(num.toString() + "\n") }
            .runWith(FileIO.toPath(Paths.get("factorials.txt")), materializer)

        //  #use-transformed-sink
        factorials
            .map(BigInteger::toString)
            .runWith(lineSink("factorial2.txt"), materializer)

        // #add-streams
        factorials
            .zipWith(Source.range(0, 99)) { num, idx -> String.format("%d! = %s", idx, num) }
            .throttle(1, Duration.ofSeconds(1))
            // #add-streams
            .take(2)
            // #add-streams
            .runForeach({ s -> println(s) }, materializer)

        // #run-source-and-terminate
        val done = source.runForeach({ i -> println(i) }, materializer)

        done.thenRun { system.terminate() }
        // #run-source-and-terminate

        done.toCompletableFuture().get()

    }

    fun lineSink(filename: String): Sink<String, CompletionStage<IOResult>> {
        return Flow.of(String::class.java)
            .map { s -> ByteString.fromString(s.toString() + "\n") }
            .toMat(FileIO.toPath(Paths.get(filename)), Keep.right())
    }

