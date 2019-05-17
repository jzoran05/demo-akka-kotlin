import akka.actor.ActorSystem
import akka.actor.PathUtils
import akka.stream.ActorMaterializer
import akka.stream.javadsl.*
import java.nio.file.Paths
import akka.stream.IOResult
import akka.util.ByteString
import java.util.concurrent.CompletionStage




class AkkaStreamFileInOut {


    fun CopyToFile() {

        val system = ActorSystem.create()
        val materializer = ActorMaterializer.create(system)

        val source = FileIO.fromPath(Paths.get("C:\\Source\\Demo\\demo-akka-kotlin\\akka-streams-sample\\src\\main\\resources\\akkastreamfilein.txt"))
        val sink = FileIO.toPath(Paths.get("sinkFile.txt"))


        val result = source.map { num -> num.toString() + "\n" }

        println("Printing source...")
        result.runForeach({ i -> println(i) }, materializer)

        val runnableGraph = source.to(sink)

        val res = runnableGraph.run(materializer)


    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AkkaStreamFileInOut().CopyToFile()
        }
    }
}

