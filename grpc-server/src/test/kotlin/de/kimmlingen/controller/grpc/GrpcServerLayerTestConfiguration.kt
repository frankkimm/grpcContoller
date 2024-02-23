package de.kimmlingen.controller.grpc

import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ControllerServiceInterface::class)
@ImportAutoConfiguration(
    classes = [
        GrpcServerAutoConfiguration::class, // Create required server beans
        GrpcServerFactoryAutoConfiguration::class, // Select server implementation
        GrpcClientAutoConfiguration::class, // Support @GrpcClient annotation
    ],
)
class GrpcServerLayerTestConfiguration {
    @Bean
    fun grpcServerService(controllerService: ControllerServiceInterface): GrpcServer {
        return GrpcServer(controllerService)
    }
}
