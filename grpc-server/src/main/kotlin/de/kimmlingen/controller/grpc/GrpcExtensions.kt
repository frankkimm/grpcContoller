/*
 * inspired (adopted) from org.onap.ccsdk.cds.blueprintsprocessor.grpc.BluePrintGrpcExtensions.kt
 */
package de.kimmlingen.controller.grpc

import io.grpc.Metadata

fun Metadata.getStringKey(key: String): String? {
    return this.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
}

fun Metadata.putStringKeyValue(
    key: String,
    value: String,
) {
    this.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value)
}
