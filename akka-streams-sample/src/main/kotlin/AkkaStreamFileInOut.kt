import akka.actor.ActorSystem
import akka.actor.PathUtils
import akka.stream.ActorMaterializer
import akka.stream.javadsl.*
import java.nio.file.Paths


class AkkaStreamFileInOut {
    fun CopyToFile() {
        val source = FileIO.fromPath(Paths.get("akkastreamfilein.txt"))
        val sink = FileIO.toPath(Paths.get("sinkFile.txt"))
        val runnableGraph = source.to(sink)

        val system = ActorSystem.create()
        val materializer = ActorMaterializer.create(system)

        runnableGraph.run(materializer)

    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AkkaStreamFileInOut().CopyToFile()
        }
    }
}

