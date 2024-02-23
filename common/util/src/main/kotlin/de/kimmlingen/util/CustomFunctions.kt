/*
 * inspired (adopted) from org.onap.ccsdk.cds.controllerblueprints.core.CustomFunctions.kt
 */
package de.kimmlingen.util

import org.slf4j.LoggerFactory
import org.slf4j.helpers.MessageFormatter
import java.util.UUID
import kotlin.reflect.KClass

fun <T : KClass<*>> logger(clazz: T) = LoggerFactory.getLogger(clazz.java)!!

fun <T : Any> T?.defaultToEmpty(): String {
    return this?.toString() ?: ""
}

fun <T : Any> T?.defaultToUUID(): String {
    return this?.toString() ?: UUID.randomUUID().toString()
}

fun String.splitCommaAsList(): List<String> {
    return this.split(",").map { it.trim() }.toList()
}

fun String.isJson(): Boolean {
    return (
        (this.trim().startsWith("{") && this.trim().endsWith("}")) ||
            (this.trim().startsWith("[") && this.trim().endsWith("]"))
    )
}

fun format(
    message: String,
    vararg args: Any?,
): String {
    if (args != null && args.isNotEmpty()) {
        return MessageFormatter.arrayFormat(message, args).message
    }
    return message
}

fun <T : Any> Map<String, *>.castOptionalValue(
    key: String,
    valueType: KClass<T>,
): T? {
    return if (containsKey(key)) {
        get(key) as? T
    } else {
        null
    }
}

inline fun checkNotEmpty(
    value: String?,
    lazyMessage: () -> Any,
): String {
    if (value == null || value.isEmpty()) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    } else {
        return value
    }
}

inline fun checkNotBlank(
    value: String?,
    lazyMessage: () -> Any,
): String {
    if (value == null || value.isBlank()) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    } else {
        return value
    }
}

fun isNotEmpty(value: String?): Boolean {
    return value != null && value.isNotEmpty()
}

fun isNotBlank(value: String?): Boolean {
    return value != null && value.isNotBlank()
}

fun <T : String> T?.emptyTONull(): String? {
    return if (this == null || this.isEmpty()) null else this
}

fun nullToEmpty(value: String?): String {
    return if (isNotEmpty(value)) value!! else ""
}
