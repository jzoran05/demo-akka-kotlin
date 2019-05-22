

import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.japi.JavaPartialFunction
import akka.japi.Pair
import akka.stream.ActorMaterializer
import akka.stream.KillSwitches
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.UniqueKillSwitch
import akka.stream.alpakka.mqtt.streaming.Command
import akka.stream.alpakka.mqtt.streaming.ConnAck
import akka.stream.alpakka.mqtt.streaming.ConnAckFlags
import akka.stream.alpakka.mqtt.streaming.ConnAckReturnCode
import akka.stream.alpakka.mqtt.streaming.Connect
import akka.stream.alpakka.mqtt.streaming.ConnectFlags
import akka.stream.alpakka.mqtt.streaming.ControlPacket
import akka.stream.alpakka.mqtt.streaming.ControlPacketFlags
import akka.stream.alpakka.mqtt.streaming.DecodeErrorOrEvent
import akka.stream.alpakka.mqtt.streaming.Event
import akka.stream.alpakka.mqtt.streaming.MqttSessionSettings
import akka.stream.alpakka.mqtt.streaming.PubAck
import akka.stream.alpakka.mqtt.streaming.Publish
import akka.stream.alpakka.mqtt.streaming.SubAck
import akka.stream.alpakka.mqtt.streaming.Subscribe
import akka.stream.alpakka.mqtt.streaming.javadsl.ActorMqttClientSession
import akka.stream.alpakka.mqtt.streaming.javadsl.ActorMqttServerSession
import akka.stream.alpakka.mqtt.streaming.javadsl.Mqtt
import akka.stream.alpakka.mqtt.streaming.javadsl.MqttClientSession
import akka.stream.alpakka.mqtt.streaming.javadsl.MqttServerSession
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Keep
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import akka.stream.javadsl.SourceQueueWithComplete
import akka.stream.javadsl.Tcp
import akka.stream.javadsl.BroadcastHub
import akka.util.ByteString
import scala.Tuple2
import scala.collection.JavaConverters
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors

class AlpakkaMqttConsumer {

    private val TIMEOUT_SECONDS = 5L

    private var system: ActorSystem? = null
    private var materializer: Materializer? = null

    private fun setupMaterializer(): Pair<ActorSystem, Materializer> {
        val system = ActorSystem.create("MqttFlowTest")
        val materializer = ActorMaterializer.create(system)
        return Pair.create(system, materializer)
    }

    fun establishClientBidirectionalConnectionAndSubscribeToATopic() {
        val clientId = "source-spec/flow"
        val topic = "source-spec/topic1"

        // #create-streaming-flow
        var settings = MqttSessionSettings.create()
        var session = ActorMqttClientSession.create(settings, materializer, system)
        var connection = Tcp.get(system).outgoingConnection("localhost", 1883)
        val run = StreamsJavaDslHelper.Run(session, system, materializer, connection)

        var commands: SourceQueueWithComplete<Command<Any>> = run.first()
        commands.offer(Command(Connect(clientId, ConnectFlags.CleanSession())))
        commands.offer(Command(Subscribe(topic)))

        val intOption = ControlPacketFlags.RETAIN() or ControlPacketFlags.QoSAtLeastOnceDelivery()
        val publish = Publish(intOption, topic, ByteString.fromString("ohi"))
        val command = Command<Any>(publish)
        session.tell(command)
        // #run-streaming-flow

        var event = run.second()
        var publishEvent = event.toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        // #run-streaming-flow

        // for shutting down properly
        commands.complete()
        commands.watchCompletion().thenAccept { session.shutdown() }
    // #run-streaming-flow
  }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AlpakkaMqttConsumer().establishClientBidirectionalConnectionAndSubscribeToATopic()
        }
    }

}

