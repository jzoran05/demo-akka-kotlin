import akka.actor.ActorSystem
//import org.testcontainers.shaded.com.fasterxml.jackson.databind.deser.std.StringDeserializer
import java.time.Clock.system


class AlpakkaKafkaConsumer {
    private val system = ActorSystem.create()
    private val config = system.settings().config().getConfig("akka.kafka.consumer")
    /*
    val consumerSettings = ConsumerSettings.create(config, StringDeserializer(), ByteArrayDeserializer())
        .withBootstrapServers("localhost:9092")
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

     */
}