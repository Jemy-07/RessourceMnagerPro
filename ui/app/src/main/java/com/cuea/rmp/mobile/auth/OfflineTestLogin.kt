package com.cuea.rmp.mobile.auth

/**
 * Offline Test Login Configuration
 *
 * SET enabled = false TO TEST WITH REAL BACKEND
 * SET enabled = true FOR UI-ONLY DEMO (no backend needed)
 */
object OfflineTestLogin {
    // ⚠️ TOGGLE THIS TO SWITCH BETWEEN MODES ⚠️
    const val enabled: Boolean = false  // Changed to false for real backend testing

    const val email: String = "tester@rmp.local"
    const val password: String = "Test1234"

    // Sentinel tokens, not real JWTs — the backend has no dev/test login endpoint that
    // issues a real token (AuthController only has register/login/refresh/logout), so
    // there's nothing for this offline shortcut to exchange for a genuine JWT. AuthInterceptor
    // recognizes this exact value and refuses to make a live backend call with it, failing
    // fast with a clear message instead of letting requests 401 and trigger
    // TokenAuthenticator's force-logout — this path is UI/navigation-only by design.
    const val sentinelAccessToken: String = "debug-access-token"
    const val sentinelRefreshToken: String = "debug-refresh-token"
}

