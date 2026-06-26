package com.cuea.rmp.mobile.core.network

import java.io.IOException

// Thrown by AuthInterceptor instead of making a live call when the active session is the
// offline test login's sentinel token. Without this, those requests would reach the real
// backend, get rejected (it's not a real JWT), and trigger TokenAuthenticator's
// force-logout — a confusing false-positive that has cost real debugging time across
// multiple sprints. This fails fast with an unambiguous message instead.
class OfflineTestSessionException : IOException(
    "This is the offline test login — it only exercises UI/navigation and never calls " +
        "the real backend. Log in with a real account to load live data."
)
