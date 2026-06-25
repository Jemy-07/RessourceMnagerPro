package com.cuea.rmp.mobile.core.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException

suspend inline fun <T> safeApiCall(
    json: Json,
    crossinline block: suspend () -> ApiResponse<T>
): T {
    return try {
        val envelope = block()
        if (envelope.success && envelope.data != null) {
            envelope.data
        } else {
            throw ApiException(
                code = envelope.errorCode,
                message = envelope.message ?: "Request failed",
                fieldErrors = emptyMap()
            )
        }
    } catch (httpException: HttpException) {
        val parsed = parseApiError(
            json = json,
            raw = httpException.response()?.errorBody()?.string(),
            fallbackMessage = "HTTP ${httpException.code()}"
        )
        throw ApiException(
            code = parsed.errorCode,
            message = parsed.message,
            statusCode = httpException.code(),
            fieldErrors = parsed.fieldErrors,
            cause = httpException
        )
    }
}

suspend inline fun safeApiCallUnit(
    json: Json,
    crossinline block: suspend () -> ApiResponse<Unit>
) {
    try {
        val envelope = block()
        if (!envelope.success) {
            throw ApiException(
                code = envelope.errorCode,
                message = envelope.message ?: "Request failed",
                fieldErrors = emptyMap()
            )
        }
    } catch (httpException: HttpException) {
        val parsed = parseApiError(
            json = json,
            raw = httpException.response()?.errorBody()?.string(),
            fallbackMessage = "HTTP ${httpException.code()}"
        )
        throw ApiException(
            code = parsed.errorCode,
            message = parsed.message,
            statusCode = httpException.code(),
            fieldErrors = parsed.fieldErrors,
            cause = httpException
        )
    }
}

