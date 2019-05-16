
import akka.NotUsed
import akka.actor.ActorSystem
import akka.japi.JavaPartialFunction

import java.time.Clock.system
import java.util.concurrent.CompletionStage

import akka.stream.Materializer
import akka.stream.ActorMaterializer
import java.time.Clock.system
import akka.japi.Pair
import akka.stream.OverflowStrategy
import akka.util.ByteString
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
import akka.stream.javadsl.*
import java.util.concurrent.TimeUnit

class AlpakkaMqttConsumer {

    private val TIMEOUT_SECONDS = 5

    private var system: ActorSystem? = null
    private var materializer: Materializer? = null


    private fun setupMaterializer(): Pair<ActorSystem, Materializer> {
        val system = ActorSystem.create("MqttFlowTest")
        val materializer = ActorMaterializer.create(system)
        return Pair.create(system, materializer)
    }

/*
    fun EstablishClientBidirectionalConnectionAndSubscribeToATopic() {

        val sysmat = setupMaterializer()
        system = sysmat.first()
        materializer = sysmat.second()

        var clientId = "source-spec/flow"
        var topic = "source-spec/topic1"

        // #create-streaming-flow
        var settings = MqttSessionSettings.create()
        var session = ActorMqttClientSession.create(settings, materializer, system)

        var connection: Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> = Tcp.get(system).outgoingConnection("localhost", 1883)

        var mqttFlow: Flow<Command<Object>, DecodeErrorOrEvent<Object>, NotUsed> = Mqtt.clientSessionFlow(session, ByteString.fromString("1")).join(connection)

    // #create-streaming-flow

        // #run-streaming-flow
        var run = Source.<Command<Object>>queue(3, OverflowStrategy.fail())
                .via(mqttFlow)
                .collect(JavaPartialFunction<DecodeErrorOrEvent<Object>, Publish>() {
                      @Override
                      Publish apply(DecodeErrorOrEvent<Object> x, boolean isCheck) {
                        if (x.getEvent().isPresent() && x.getEvent().get().event() instanceof Publish)
                          return (Publish) x.getEvent().get().event();
                        else throw noMatch();
                      }
                    })
                .toMat(Sink.head(), Keep.both())
                .run(materializer)

        SourceQueueWithComplete<Command<Object>> commands = run.first()
        commands.offer(Command<>(Connect(clientId, ConnectFlags.CleanSession())))
        commands.offer(Command<>(Subscribe(topic)));
        session.tell(
            Command<>(
                Publish(
                    ControlPacketFlags.RETAIN() | ControlPacketFlags.QoSAtLeastOnceDelivery(),
                    topic,
                    ByteString.fromString("ohi"))))
    // #run-streaming-flow

        CompletionStage<Publish> event = run.second();
        Publish publishEvent = event.toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertEquals(publishEvent.topicName(), topic);
        assertEquals(publishEvent.payload(), ByteString.fromString("ohi"))

    // #run-streaming-flow

    // for shutting down properly
    commands.complete();
    commands.watchCompletion().thenAccept(done -> session.shutdown());
    // #run-streaming-flow
  }
*/
}