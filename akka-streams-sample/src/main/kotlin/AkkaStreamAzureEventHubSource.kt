import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.*
import akka.stream.javadsl.*
import java.util.concurrent.CompletionStage
import akka.stream.IOResult

import com.microsoft.azure.eventhubs.*
import java.util.concurrent.Executors
import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.eventhubs.EventPosition
import com.microsoft.azure.eventhubs.PartitionReceiver
import java.time.Duration
import com.microsoft.azure.eventhubs.EventData
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class AkkaStreamAzureEventHubSource {

    fun receive(sasKey: String) {

        val sys = ActorSystem.create()
        val mat = ActorMaterializer.create(sys)

        val sink = Sink.fromGraph(AkkaStreamPrintlnSink() )
        val source = Source.fromGraph(AkkaStreamSourceAzureEventHub(
            "iothub-ns-csucsa-iot-1356663-1595b19cba",
            "csucsa-iot-demo",
            "iothubowner",
             sasKey,
            "2" ) )

        source.runForeach( { i:EventData -> println(i) }, mat)
        val runnable = source.alsoTo(sink)
        runnable.runForeach( { i -> println(i) }, mat )
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AkkaStreamAzureEventHubSource().receive(args[0])
        }
    }

}