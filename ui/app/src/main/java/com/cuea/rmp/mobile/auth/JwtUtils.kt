package com.cuea.rmp.mobile.auth

import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// No backend /me endpoint exists to identify the logged-in user — JwtTokenProvider.java
// already puts the user id in the standard JWT "sub" claim, so we decode it client-side
// rather than round-tripping to the server. No signature verification needed: the token
// is only trusted to the same degree it's already trusted as a bearer credential.
object JwtUtils {

    fun extractUserId(accessToken: String): String? {
        val parts = accessToken.split(".")
        if (parts.size < 2) return null

        return try {
            val payload = String(decodeBase64Url(parts[1]))
            Json.parseToJsonElement(payload).jsonObject["sub"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeBase64Url(value: String): ByteArray {
        val padded = when (value.length % 4) {
            2 -> "$value=="
            3 -> "$value="
            else -> value
        }
        return Base64.getUrlDecoder().decode(padded)
    }
}
