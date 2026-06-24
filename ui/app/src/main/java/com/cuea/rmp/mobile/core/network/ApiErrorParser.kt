package com.cuea.rmp.mobile.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
private data class ErrorEnvelope(
    val success: Boolean = false,
    val message: String? = null,
    val errorCode: String? = null,
    val data: JsonObject? = null
)

data class ParsedApiError(
    val message: String,
    val errorCode: String?,
    val fieldErrors: Map<String, String>
)

fun parseApiError(json: Json, raw: String?, fallbackMessage: String): ParsedApiError {
    if (raw.isNullOrBlank()) {
        return ParsedApiError(fallbackMessage, null, emptyMap())
    }

    return try {
        val envelope = json.decodeFromString<ErrorEnvelope>(raw)
        val fieldErrors = envelope.data
            ?.mapNotNull { (key, value) ->
                val str = value.jsonPrimitive.contentOrNull ?: return@mapNotNull null
                key to str
            }
            ?.toMap()
            .orEmpty()

        ParsedApiError(
            message = envelope.message ?: fallbackMessage,
            errorCode = envelope.errorCode,
            fieldErrors = fieldErrors
        )
    } catch (_: Exception) {
        ParsedApiError(fallbackMessage, null, emptyMap())
    }
}

