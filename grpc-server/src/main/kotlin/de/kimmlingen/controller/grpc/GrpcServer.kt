package de.kimmlingen.controller.grpc

import de.kimmlingen.controller.grpc.model.Request
import de.kimmlingen.controller.grpc.model.Response
import de.kimmlingen.controller.grpc.model.ServiceGrpc
import de.kimmlingen.util.logger
import io.grpc.Status
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.server.service.GrpcService
import org.apache.commons.lang3.exception.ExceptionUtils

fun Exception.errorCauseOrDefault(): Throwable {
    return ExceptionUtils.getRootCause(this)
}

fun handleError(
    error: Exception,
    responseObserver: StreamObserver<Response>,
) {
    responseObserver.onError(
        Status.INTERNAL
            .withDescription(error.message)
            .withCause(error.errorCauseOrDefault())
            .asException(),
    )
}

@GrpcService(interceptors = [GrpcServerLoggingInterceptor::class])
class GrpcServer(val controllerService: ControllerServiceInterface) : ServiceGrpc.ServiceImplBase() {
    companion object {
        private val log = logger(GrpcServer::class)
    }

    override fun send(
        request: Request,
        responseObserver: StreamObserver<Response>,
    ) {
        runBlocking {
            mdcGrpcCoroutineScope(request) {
                try {
                    log.info(toString(request))
                    val response = controllerService.send(request)
                    responseObserver.onNext(response)
                    responseObserver.onCompleted()
                } catch (e: Exception) {
                    // implement error-handling
                    handleError(e, responseObserver)
                }
            }
        }
    }

    override fun streamMultipleRequests(responseObserver: StreamObserver<Response>): StreamObserver<Request> {
        return object : StreamObserver<Request> {
            override fun onNext(request: Request) {
                try {
                    runBlocking {
                        mdcGrpcCoroutineScope(request) {
                            log.info(toString(request))
                            controllerService.stream(
                                request,
                                responseObserver,
                                false,
                            )
                        }
                    }
                } catch (e: Exception) {
                    // implement error-handling
                    handleError(e, responseObserver)
                }
            }

            override fun onError(error: Throwable) {
                log.error("Terminating stream error:", error)
                responseObserver.onCompleted()
            }

            override fun onCompleted() {
                log.info("Completed")
                // close server-stream only if client signals completion
                responseObserver.onCompleted()
            }
        }
    }

    override fun streamSingleRequest(
        request: Request,
        responseObserver: StreamObserver<Response>,
    ) {
        runBlocking {
            try {
                mdcGrpcCoroutineScope(request) {
                    log.info(toString(request))
                    controllerService.stream(
                        request,
                        responseObserver,
                        true,
                    )
                }
            } catch (e: Exception) {
                // implement error-handling
                handleError(e, responseObserver)
            }
        }
    }
}

fun toString(request: Request): String {
    return "org:${request.org} requestId: ${request.commonHeader.requestId}"
}
