/*
 * inspired (adopted) from org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.GrpcLoggerService
 */
package de.kimmlingen.controller.grpc

import de.kimmlingen.controller.grpc.common.CommonHeader
import de.kimmlingen.controller.grpc.model.Request
import de.kimmlingen.util.Constants
import de.kimmlingen.util.Constants.INVOCATION_ID
import de.kimmlingen.util.Constants.PARTNER_NAME
import de.kimmlingen.util.Constants.REQUEST_ID
import de.kimmlingen.util.MDCContext
import de.kimmlingen.util.defaultToEmpty
import de.kimmlingen.util.defaultToUUID
import de.kimmlingen.util.logger
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import java.net.InetAddress
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class GrpcLoggerService {
    private val log = logger(GrpcLoggerService::class)

    /** Used when server receives request */
    fun <ReqT : Any, RespT : Any> grpcRequesting(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>,
    ) {
        val requestID = headers.getStringKey(REQUEST_ID).defaultToUUID()
        val invocationID = headers.getStringKey(INVOCATION_ID).defaultToUUID()
        val partnerName = headers.getStringKey(PARTNER_NAME) ?: "UNKNOWN"
        grpcRequesting(requestID, invocationID, partnerName, call)
    }

    fun <ReqT : Any, RespT : Any> grpcRequesting(
        call: ServerCall<ReqT, RespT>,
        headers: CommonHeader,
        next: ServerCallHandler<ReqT, RespT>,
    ) {
        val requestID = headers.requestId.defaultToUUID()
        val invocationID = headers.subRequestId.defaultToUUID()
        val partnerName = headers.originatorId ?: "UNKNOWN"
        grpcRequesting(requestID, invocationID, partnerName, call)
    }

    fun <ReqT : Any, RespT : Any> grpcRequesting(
        requestID: String,
        invocationID: String,
        partnerName: String,
        call: ServerCall<ReqT, RespT>,
    ) {
        val localhost = InetAddress.getLocalHost()

        val serviceName = call.methodDescriptor.fullMethodName

        MDC.put("InvokeTimestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
        MDC.put("RequestID", requestID)
        MDC.put("InvocationID", invocationID)
        MDC.put("PartnerName", partnerName)
        MDC.put("ServerFQDN", localhost.hostName.defaultToEmpty())
        MDC.put("ServiceName", serviceName)
        log.trace("MDC Properties : ${MDC.getCopyOfContextMap()}")
    }

    /** Used before invoking any GRPC outbound request, Inbound Invocation ID is used as request Id
     * for outbound Request, If invocation Id is missing then default Request Id will be generated.
     */
    fun grpcInvoking(requestHeader: Metadata) {
        requestHeader.putStringKeyValue(REQUEST_ID, MDC.get("InvocationID").defaultToUUID())
        requestHeader.putStringKeyValue(INVOCATION_ID, UUID.randomUUID().toString())
        requestHeader.putStringKeyValue(PARTNER_NAME, Constants.APP_NAME)
    }

    /** Used when server returns response */
    fun grpResponding(
        requestHeaders: Metadata,
        responseHeaders: Metadata,
    ) {
        try {
            responseHeaders.putStringKeyValue(REQUEST_ID, MDC.get("RequestID").defaultToEmpty())
            responseHeaders.putStringKeyValue(INVOCATION_ID, MDC.get("InvocationID").defaultToEmpty())
            responseHeaders.putStringKeyValue(PARTNER_NAME, MDC.get("PartnerName").defaultToEmpty())
        } catch (e: Exception) {
            log.warn("couldn't set grpc response headers", e)
        }
    }
}

suspend fun <T> mdcGrpcCoroutineScope(
    request: Request,
    block: suspend CoroutineScope.() -> T,
) = coroutineScope {
    MDC.put("RequestID", request.commonHeader.requestId)
    MDC.put("SubRequestID", request.commonHeader.subRequestId)
    MDC.put("OriginatorID", request.commonHeader.originatorId)

    withContext(
        newCoroutineContext(
            this.coroutineContext +
                MDCContext(MDC.getCopyOfContextMap()),
        ),
    ) {
        block()
    }
}
