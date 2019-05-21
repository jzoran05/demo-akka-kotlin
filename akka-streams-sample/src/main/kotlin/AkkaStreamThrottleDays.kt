import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Source
import java.time.Duration.ofSeconds

/**
 *  Basic Akka Stream sample which use time as a source and throttle and maps by using the flow
 */
class AkkaStreamThrottleDays {


    fun start() {

        val sys = ActorSystem.create()
        val mat = ActorMaterializer.create(sys)

        val greetingFlow =
            Flow.of(String::class.java)
                .throttle(1, ofSeconds(5) )
                .map { name -> "hello, $name" }

        Source
            .tick(
                ofSeconds(1),
                ofSeconds(1),
                "Zoran"
            )
            .via(greetingFlow)
            .runForeach({ r -> println(r) }, mat)

    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            AkkaStreamThrottleDays().start()
        }
    }

}