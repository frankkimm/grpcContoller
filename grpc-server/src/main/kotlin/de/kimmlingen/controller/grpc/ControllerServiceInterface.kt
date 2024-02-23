package de.kimmlingen.controller.grpc

import de.kimmlingen.controller.grpc.model.Request
import de.kimmlingen.controller.grpc.model.Response
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Job

/**
 * Interface for the controller service
 */
interface ControllerServiceInterface {
    /**
     * Stream the response to the client (asynchronous)
     */
    suspend fun stream(
        request: Request,
        responseObserver: StreamObserver<Response>,
        withCompletion: Boolean,
    ): Job

    /**
     * Send the response to the client (synchronous)
     */
    suspend fun send(request: Request): Response
}
