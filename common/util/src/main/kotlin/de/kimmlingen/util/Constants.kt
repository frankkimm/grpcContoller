package de.kimmlingen.util

object Constants {
    val APP_NAME =
        System.getenv("APP_NAME")
            ?: "grpc-controller"
    const val REQUEST_ID = "X-RequestID"
    const val INVOCATION_ID = "X-InvocationID"
    const val PARTNER_NAME = "X-PartnerName"
}
