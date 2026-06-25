package com.cuea.rmp.mobile.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class ApiErrorParserTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun `parses validation field errors`() {
        val raw = """
            {
              "success": false,
              "message": "Validation failed",
              "errorCode": "VALIDATION_ERROR",
              "data": {
                "email": "must be a well-formed email address",
                "password": "size must be between 8 and 100"
              }
            }
        """.trimIndent()

        val parsed = parseApiError(json, raw, fallbackMessage = "fallback")

        assertEquals("Validation failed", parsed.message)
        assertEquals("VALIDATION_ERROR", parsed.errorCode)
        assertEquals(2, parsed.fieldErrors.size)
        assertEquals("must be a well-formed email address", parsed.fieldErrors["email"])
    }

    @Test
    fun `returns fallback for invalid json`() {
        val parsed = parseApiError(json, "not-json", fallbackMessage = "fallback")

        assertEquals("fallback", parsed.message)
        assertEquals(null, parsed.errorCode)
        assertTrue(parsed.fieldErrors.isEmpty())
    }
}

