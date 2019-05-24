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
import akka.stream.stage.AbstractOutHandler
import akka.stream.stage.GraphStageLogic
import akka.stream.SourceShape
import akka.stream.Outlet
import akka.stream.stage.GraphStage
import akka.*
import java.*

class AkkaStreamSourceAzureEventHub(NamespaceName: String, EventHubName: String, SasKeyName: String, SaSKey: String, PartitionId: String) : GraphStage<SourceShape<EventData>>() {

    val namespaceName = NamespaceName
    val eventHubName = EventHubName
    val sasKeyName = SasKeyName
    val sasKey = SaSKey
    val partitionId = PartitionId

    val out = Outlet.create<EventData>("EventData.out")
    private val shape = SourceShape.of(out)
    private val executor = Executors.newScheduledThreadPool(8)

    override fun shape(): SourceShape<EventData> {
        return shape
    }

    override fun addAttributes(attr: Attributes?): Graph<SourceShape<EventData>, NotUsed> {
        return super.addAttributes(attr)
    }


    override fun createLogic(inheritedAttributes: Attributes): GraphStageLogic {


        return object : GraphStageLogic(shape) {
            private val random = Random()
            private var receiver: PartitionReceiver? = null
            private var ehClient: EventHubClient? = null

            override fun postStop() {

                receiver?.close()
                ehClient?.close()
                super.postStop()
            }

            override fun preStart() {

                val connStr = ConnectionStringBuilder()
                    .setNamespaceName(namespaceName)
                    .setEventHubName(eventHubName)
                    .setSasKeyName(sasKeyName)
                    .setSasKey(sasKey)

                ehClient = EventHubClient.create(connStr.toString(), executor) as EventHubClient

                //val eventHubInfo = ehClient.getRuntimeInformation().get()
                //val partitionId = eventHubInfo.getPartitionIds()[2] // get first partition's id

                receiver = ehClient?.createEpochReceiverSync (
                    EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
                    partitionId,
                    EventPosition.fromStartOfStream(),
                    2345L
                )

                super.preStart()
            }

            init {
                setHandler(out, object : AbstractOutHandler() {
                    @Throws(Exception::class)
                    override fun onPull() {

                        receiver?.receive(1)?.thenAcceptAsync {
                                data -> data.forEach { event -> push(out, event) }
                            }
                    }
                })
            }
        }
    }
}
