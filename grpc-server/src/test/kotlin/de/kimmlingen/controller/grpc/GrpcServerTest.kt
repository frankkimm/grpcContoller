package de.kimmlingen.controller.grpc

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.kimmlingen.controller.grpc.model.Request
import de.kimmlingen.controller.grpc.model.Response
import de.kimmlingen.controller.grpc.model.ServiceGrpc
import de.kimmlingen.util.logger
import io.grpc.Channel
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest(
    properties = [
        "grpc.server.inProcessName=test",
        "grpc.server.port=-1",
        "grpc.client.inProcess.address=in-process:test",
    ],
)
@SpringJUnitConfig(classes = [GrpcServerLayerTestConfiguration::class])
@DirtiesContext
class GrpcServerTest {
    companion object {
        // To see a difference in performance set REQUESTS to 5000
        private const val REQUESTS = 5
        private val log = logger(GrpcServerTest::class)
    }

    @MockBean
    val controllerService: ControllerServiceInterface? = null

    @GrpcClient("inProcess")
    val service: ServiceGrpc.ServiceBlockingStub? = null

    @Test
    fun shouldSend() =
        runTest {
            whenever(controllerService?.send(any()))
                .thenReturn(
                    Response.newBuilder()
                        .setOrg("kotlin")
                        .setResponseBody("test-response")
                        .build(),
                )

            val response =
                service?.send(
                    Request.newBuilder()
                        .setOrg("org")
                        .build(),
                )
            val respBody: String? = response?.responseBody
            assertNotNull(respBody)
            assertEquals("test-response", respBody)
        }

    @GrpcClient("inProcess")
    lateinit var channel: Channel

    @Test
    fun shouldStreamMultipleRequests() =
        runTest {
            // Create a latch to wait for completion
            val latch = CountDownLatch(1)
            whenever(controllerService?.stream(any(), any(), any()))
                .thenReturn(Job())

            val responseObserver =
                object : StreamObserver<Response> {
                    private var responseCount = 0
                    private val responses = mutableListOf<Response>()

                    override fun onNext(response: Response) {
                        // Process incoming responses in the test
                        log.info("Received response in test: ${response.org}")
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
                Request.newBuilder()
                    .setOrg("org")
                    .build(),
            )
            requestObserver.onNext(
                Request.newBuilder()
                    .setOrg("org")
                    .build(),
            )

            // client signals completion (no additional requests)
            requestObserver.onCompleted()

            // used to wait for both streaming methods to complete before performing assertions
            assertTrue(latch.await(10, TimeUnit.SECONDS))
            verify(controllerService, atLeast(2))?.stream(any(), any(), any())
        }

    @Test
    fun shouldStreamSingleRequests() =
        runTest {
            // Create a latch to wait for completion
            val latch = CountDownLatch(1)

            // mockito: call method responseObserver.onCompleted() on second parameter
            whenever(controllerService?.stream(any(), any(), any()))
                .doAnswer {
                    val responseObserver = it.arguments[1] as StreamObserver<*>
                    responseObserver.onCompleted()
                    Job()
                }

            val responseObserver =
                object : StreamObserver<Response> {
                    private var responseCount = 0
                    private val responses = mutableListOf<Response>()

                    override fun onNext(response: Response) {
                        // Process incoming responses in the test
                        log.info("Received response in test: ${response.org}")
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

            val stub = ServiceGrpc.newStub(channel)

            val request =
                Request.newBuilder()
                    .setOrg("org")
                    .build()
            stub.streamSingleRequest(request, responseObserver)

            assertTrue(latch.await(10, TimeUnit.SECONDS))
            verify(controllerService)?.stream(any(), any(), any())
        }
}
