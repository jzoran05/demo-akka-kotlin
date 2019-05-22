import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.JavaPartialFunction;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.alpakka.mqtt.streaming.Command;
import akka.stream.alpakka.mqtt.streaming.DecodeErrorOrEvent;
import akka.stream.alpakka.mqtt.streaming.Publish;
import akka.stream.alpakka.mqtt.streaming.javadsl.Mqtt;
import akka.stream.alpakka.mqtt.streaming.javadsl.MqttClientSession;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;

public class StreamsJavaDslHelper {


    public static Pair<SourceQueueWithComplete<Command<Object>>, CompletionStage<Publish>> Run(
            MqttClientSession session,
            ActorSystem system,
            Materializer materializer,
            Flow<ByteString, ByteString, CompletionStage<Tcp.OutgoingConnection>> connection) {


        Flow<Command<Object>, DecodeErrorOrEvent<Object>, NotUsed> mqttFlow = Mqtt.clientSessionFlow(session, ByteString.fromString("1")).join(connection);

        return  Source.<Command<Object>>queue(3, OverflowStrategy.fail())
                .via(mqttFlow)
                .collect(
                        new JavaPartialFunction<DecodeErrorOrEvent<Object>, Publish>() {
                            @Override
                            public Publish apply(DecodeErrorOrEvent<Object> x, boolean isCheck) {
                                if (x.getEvent().isPresent() && x.getEvent().get().event() instanceof Publish)
                                    return (Publish) x.getEvent().get().event();
                                else throw noMatch();
                            }
                        })
                .toMat(Sink.head(), Keep.both())
                .run(materializer);
    }
}
