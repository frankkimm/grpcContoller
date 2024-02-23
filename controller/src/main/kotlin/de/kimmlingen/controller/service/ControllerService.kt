package de.kimmlingen.controller.service

import de.kimmlingen.controller.grpc.ControllerServiceInterface
import de.kimmlingen.controller.grpc.model.Request
import de.kimmlingen.controller.grpc.model.Response
import de.kimmlingen.controller.grpc.toString
import de.kimmlingen.github.MockGithubService
import de.kimmlingen.github.RequestData
import de.kimmlingen.github.createGitHubService
import de.kimmlingen.github.loadContributorsChannels
import de.kimmlingen.util.logger
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class ControllerService() : ControllerServiceInterface {
    companion object {
        private val log = logger(ControllerService::class)
    }

    override suspend fun stream(
        request: Request,
        responseObserver: StreamObserver<Response>,
        withCompletion: Boolean,
    ) = coroutineScope {
        log.info(toString(request))
        val service =
            if (request.isGitHubMocked) {
                MockGithubService
            } else {
                createGitHubService(request.username, request.password)
            }

        launch(Dispatchers.Default) {
            loadContributorsChannels(
                service,
                RequestData(request.username, request.password, request.org),
            ) {
                    users, completed ->
                val response =
                    Response.newBuilder()
                        .setResponseBody(
                            if (completed) {
                                users.toString()
                            } else {
                                users.size.toString()
                            },
                        )
                        .setOrg(request.org)
                        .build()
                responseObserver.onNext(response)
                if (withCompletion && completed) {
                    responseObserver.onCompleted()
                }
            }
        }
    }

    override suspend fun send(request: Request): Response =
        coroutineScope {
            log.info(toString(request))
            val service =
                if (request.isGitHubMocked) {
                    MockGithubService
                } else {
                    createGitHubService(request.username, request.password)
                }

            var response: Response? = null
            val job =
                launch(Dispatchers.Default) {
                    loadContributorsChannels(
                        service,
                        RequestData(request.username, request.password, request.org),
                    ) { users, completed ->
                        if (completed) {
                            response =
                                Response.newBuilder().setResponseBody(users.toString())
                                    .setOrg(request.org)
                                    .build()
                        }
                    }
                }
            job.join()
            response!!
        }
}
