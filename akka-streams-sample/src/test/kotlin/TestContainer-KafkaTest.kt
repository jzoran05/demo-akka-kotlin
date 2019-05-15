import java.time.Duration
import org.testcontainers.containers.KafkaContainer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows

import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class TestContainerKafka  {


    @Container
    val kafka = KafkaContainer()

    @Test
    fun testSomeActor() {
        var servers = kafka.bootstrapServers

        assertTrue(servers.isNullOrBlank() == false)
        val running = kafka.isRunning

        assertTrue(running)
    }

/*
    @BeforeAll
    fun setup() {

    }

    @AfterAll
    fun teardown() {

    }
*/

}