// From: https://learning.oreilly.com/library/view/akka-cookbook/9781785288180/096647d2-2a14-4b80-a3ff-ea13a447ee7e.xhtml

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source

/**
 * Sample class to demonstrate:
 * - parsing list of files
 * - filtering file list by file exist, file not empty
 *
 * @see
 */
class AkkaStreamFileFilter {

    private fun FilterFiles() {

        val actorSystem = ActorSystem.create("SimpleStream")
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val fileList = listOf(
            "src/main/resources/testfile1.text",
            "src/main/resources/testfile2.txt",
            "src/main/resources/testfile3.txt")

        val source = Source.from(fileList)

        val stream = source
            .map({name -> java.io.File(name)})
            .filter({e -> e.exists()})
            .filter({l -> l.length() != 0L})

        println("runnable starting...")
        val runnable = stream.to(Sink.foreach({f -> println("Absolute path: ${f.getAbsolutePath()}") } ))
        val done = runnable.run(actorMaterializer)
        done.run { actorSystem.terminate() }
        println("runnable completed...")
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AkkaStreamFileFilter().FilterFiles()
        }
    }
}