import akka.actor.ActorSystem
import akka.actor.PathUtils
import akka.stream.ActorMaterializer
import akka.stream.javadsl.*
import java.nio.file.Paths
import akka.stream.IOResult
import akka.util.ByteString
import java.util.concurrent.CompletionStage
import akka.NotUsed
import java.math.BigInteger
import akka.japi.JAPI.seq
import java.util.concurrent.TimeUnit
import akka.stream.javadsl.FramingTruncation




class AkkaStreamFileInOut {


    fun CopyToFile() {

        val system = ActorSystem.create()
        val materializer = ActorMaterializer.create(system)

        val source = FileIO.fromPath(Paths.get("C:\\Source\\Demo\\demo-akka-kotlin\\akka-streams-sample\\src\\main\\resources\\akkastreamfilein.txt"))
        val sink = FileIO.toPath(Paths.get("sinkFile.txt"))


        val lines = source
            .via(Framing.delimiter(ByteString.fromString("\r\n"), 100, FramingTruncation.ALLOW))
            .map({ b -> b.utf8String() })

        lines.runForeach( { i -> println(i)}, materializer)

        val runnableGraph = source.to(sink)

        //println("Running graphs")
        val res = runnableGraph.run(materializer)


    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AkkaStreamFileInOut().CopyToFile()
        }
    }
}

