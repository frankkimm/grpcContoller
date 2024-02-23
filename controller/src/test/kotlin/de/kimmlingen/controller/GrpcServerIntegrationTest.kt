package de.kimmlingen.controller

import de.kimmlingen.controller.grpc.common.CommonHeader
import de.kimmlingen.controller.grpc.model.Request
import de.kimmlingen.controller.grpc.model.Response
import de.kimmlingen.controller.grpc.model.ServiceGrpc
import de.kimmlingen.util.logger
import io.grpc.Channel
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@SpringBootTest(
    properties = [
        "grpc.server.inProcessName=test",
        "grpc.server.port=-1",
        "grpc.client.inProcess.address=in-process:test",
    ],
)
@SpringJUnitConfig(classes = [GrpcServerIntegrationTestConfiguration::class])
@DirtiesContext
class GrpcServerIntegrationTest {
    companion object {
        private val log = logger(GrpcServerIntegrationTest::class)

        // To see a difference in performance set REQUESTS to 5000
        private const val REQUESTS = 5
        private const val TIMEOUT: Int = 200

        // Use MockGithubService to avoid rate limiting of GitHub API
        private const val IS_GITHUB_MOCKED = true
        private val KOTLIN_REPOS: Int =
            if (IS_GITHUB_MOCKED) {
                3
            } else {
                93
            }
    }

    @GrpcClient("inProcess")
    val service: ServiceGrpc.ServiceBlockingStub? = null

    private fun createRequest(): Request =
        Request.newBuilder()
            .setUsername("TODO: Your GitHub username")
            .setPassword("TODO: Your GitHub password / TOKEN")
            .setOrg("kotlin")
            .setIsGitHubMocked(IS_GITHUB_MOCKED)
            .setCommonHeader(
                CommonHeader.newBuilder()
                    .setRequestId("requestId")
                    .setSubRequestId("subRequestId")
                    .setOriginatorId("originatorId")
                    .setTimestamp("Timestamp")
                    .build(),
            )
            .build()

    @Test
    fun shouldSend() {
        val response = send()
        val respBody: String? = response?.responseBody
        assertNotNull(respBody)
    }

    private fun send(): Response? {
        val response =
            service?.send(
                createRequest(),
            )
        if (response != null) {
            log.info("Received response in test: org:${response?.org} body:${response.responseBody}")
        }
        return response
    }

    @GrpcClient("inProcess")
    lateinit var channel: Channel

    @Test
    fun shouldStreamMultipleRequests() {
        streamMultipleRequests()
    }

    private fun streamMultipleRequests() {
        // Create a latch to wait for completion
        val latch = CountDownLatch(1)

        val responseObserver =
            object : StreamObserver<Response> {
                private var responseCount = 0
                private val responses = mutableListOf<Response>()

                override fun onNext(response: Response) {
                    // Process incoming responses in the test
                    log.info("Received response in test: org:${response.org} body:${response.responseBody}")
                    responseCount++
                    responses.add(response)
                }

                override fun onError(t: Throwable) {
                    // Handle errors in the test
                    log.info("Error in test: ${t.message}")
                    latch.countDown()
                }

                override fun onCompleted() {
                    // Complete the bidirectional stream in the test
                    log.info("Streaming completed in test")
                    latch.countDown()
                }

                fun getResponseCount(): Int {
                    return responseCount
                }

                fun getResponses(): List<Response> {
                    return responses
                }
            }

        val requestObserver = ServiceGrpc.newStub(channel).streamMultipleRequests(responseObserver)

        // Simulate sending requests
        requestObserver.onNext(
            createRequest(),
        )
        requestObserver.onNext(
            createRequest(),
        )

        // client signals completion (no additional requests)
        requestObserver.onCompleted()

        // used to wait for both streaming methods to complete before performing assertions
        assertTrue(latch.await(20, TimeUnit.SECONDS))
        assertEquals(KOTLIN_REPOS * 2, responseObserver.getResponseCount())
    }

    @Test
    fun shouldStreamSingleRequest() {
        streamSingleRequest()
    }

    private fun streamSingleRequest() {
        // Create a latch to wait for completion
        val latch = CountDownLatch(1)

        val responseObserver =
            object : StreamObserver<Response> {
                private var responseCount = 0
                private val responses = mutableListOf<Response>()

                override fun onNext(response: Response) {
                    // Process incoming responses in the test
                    log.info("Received response in test: org:${response.org} body:${response.responseBody}")
                    responseCount++
                    responses.add(response)
                }

                override fun onError(t: Throwable) {
                    // Handle errors in the test
                    log.info("Error in test: ${t.message}")
                    latch.countDown()
                }

                override fun onCompleted() {
                    // Server completes streaming with after sending all responses
                    log.info("Streaming completed in test")
                    latch.countDown()
                }

                fun getResponseCount(): Int {
                    return responseCount
                }

                fun getResponses(): List<Response> {
                    return responses
                }
            }

        ServiceGrpc.newStub(channel).streamSingleRequest(createRequest(), responseObserver)

        // used to wait for both streaming methods to complete before performing assertions
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        assertEquals(KOTLIN_REPOS, responseObserver.getResponseCount())
    }

    @Test
    fun loadTestSend() =
        runTest(timeout = TIMEOUT.seconds) {
            val time =
                measureTimeMillis {
                    withContext(Dispatchers.IO) {
                        repeat(REQUESTS) {
                            launch {
                                val response = send()
                                val respBody: String? = response?.responseBody
                                assertNotNull(respBody)
                            }
                        }
                    }
                }

            log.info("All requests send away after ms: $time")
        }

    @Test
    fun loadTestStreamSingleRequest() =
        runTest(timeout = TIMEOUT.seconds) {
            val time =
                measureTimeMillis {
                    withContext(Dispatchers.IO) {
                        repeat(REQUESTS) {
                            launch {
                                streamSingleRequest()
                            }
                        }
                    }
                }

            log.info("All requests send away after ms: $time")
        }

    @Test
    fun loadTestStreamMultipleRequests() =
        runTest(timeout = TIMEOUT.seconds) {
            val time =
                measureTimeMillis {
                    withContext(Dispatchers.IO) {
                        repeat(REQUESTS) {
                            launch {
                                streamMultipleRequests()
                            }
                        }
                    }
                }

            log.info("All requests send away after ms: $time")
        }
}
