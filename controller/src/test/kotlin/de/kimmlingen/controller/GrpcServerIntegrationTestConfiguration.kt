package de.kimmlingen.controller

import de.kimmlingen.controller.grpc.GrpcServer
import de.kimmlingen.controller.service.ControllerService
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ControllerService::class, GrpcServer::class)
@ImportAutoConfiguration(
    classes = [
        GrpcServerAutoConfiguration::class, // Create required server beans
        GrpcServerFactoryAutoConfiguration::class, // Select server implementation
        GrpcClientAutoConfiguration::class, // Support @GrpcClient annotation
    ],
)
class GrpcServerIntegrationTestConfiguration
