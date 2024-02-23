package de.kimmlingen.controller.rest

import de.kimmlingen.util.ThreadInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import java.util.concurrent.Executors
import java.util.function.IntConsumer
import java.util.stream.IntStream
import kotlin.system.measureTimeMillis
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

// Virtual threads enabled in application.yaml
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Ignore
class ThreadInfoControllerTest {
    companion object {
        private val log = LoggerFactory.getLogger(ThreadInfoControllerTest::class.java)

        // To see a difference in performance set REQUESTS to 5000
        private const val REQUESTS = 5
    }

    private val client = RestTemplate()

    @Test
    @Order(1)
    fun loadTestWithVirtualThreads() {
        val time =
            measureTimeMillis {
                Executors.newVirtualThreadPerTaskExecutor().use { executor ->
                    IntStream.range(0, REQUESTS).forEach(
                        IntConsumer { _: Int ->
                            executor.submit(
                                Runnable {
                                    val resp = client.getForObject("http://localhost:8080/thread", ThreadInfo::class.java)
                                    log.info("Completed: $resp")
                                },
                            )
                        },
                    )
                }
            }
        log.info("All requests send away after ms: $time")
    }

    @Test
    @Order(2)
    fun loadTestWithCoroutines() =
        runTest(timeout = 20.seconds) {
            val time =
                measureTimeMillis {
                    withContext(Dispatchers.IO) {
                        repeat(REQUESTS) {
                            launch {
                                val resp = client.getForObject("http://localhost:8080/thread", ThreadInfo::class.java)
                                log.info("Completed: $resp")
                            }
                        }
                    }
                }

            log.info("All requests send away after ms: $time")
        }
}
