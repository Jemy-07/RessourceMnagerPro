package com.cuea.rmp.mobile.core.network

class ApiException(
    val code: String? = null,
    override val message: String,
    val statusCode: Int? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    cause: Throwable? = null
) : Exception(message, cause)

