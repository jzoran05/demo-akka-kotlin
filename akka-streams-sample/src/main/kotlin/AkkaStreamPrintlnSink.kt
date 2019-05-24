import akka.stream.Attributes
import akka.stream.Inlet
import akka.stream.SinkShape
import akka.stream.stage.AbstractInHandler
import akka.stream.stage.AbstractOutHandler
import akka.stream.stage.GraphStage
import akka.stream.stage.GraphStageLogic
import com.microsoft.azure.eventhubs.EventData
import org.apache.qpid.proton.engine.BaseHandler.setHandler


class AkkaStreamPrintlnSink: GraphStage<SinkShape<EventData>>() {
    val inlet: Inlet<EventData> = Inlet.create("PrintlnSink.in")

    // safe to keep immutable state within the GraphStage itself:
    // mutable state should be kept in the GraphStageLogic instance.
    var prefix: String? = null

    fun PrintlnSink(prefix: String) {
        this.prefix = prefix
    }

    override fun shape(): SinkShape<EventData> {
        return SinkShape.of(inlet)
    }

    override fun createLogic(inheritedAttributes: Attributes): GraphStageLogic {

        return object : GraphStageLogic(shape()) {
            // a new GraphStageLogic will be created each time we materialize the sink

            // so all state that we want to keep for the stage should be within the logic.
            //
            // here we maintain a counter of how many messages we printed:
            private var count = 0L

            init {
                setHandler(inlet, object : AbstractInHandler() {

                    @Throws(Exception::class)
                    override fun onPush() {
                        // We grab the element from the input port.
                        // Notice that it is properly typed as a String.
                        //
                        // Another reason we explicitly `grab` elements that sometimes one is not
                        // immediately ready to consume an input and this is basically a buffer space of one for free.
                        val element = grab(inlet)

                        // Since the GraphStage maintains the Actor-like single-threaded illusion we can safely mutate
                        // our internal counter, even though the stage could be running on different threads.
                        count += 1

                        // We print our message:
                        System.out.println(String.format("[%s:%d] %s", prefix, count, element));

                        // And signal that we'd like to receive more elements from the `in` port by pulling it:
                        pull(inlet)

                        // It is important to not pull a port "twice", that would lead to bugs so Akka Streams
                        // prevents you from making this mistake and would throw
                    }
                })
            }

            override fun preStart() {
                // initiate the flow of data by issuing a first pull on materialization:
                pull(inlet)
            }
        }
    }

}