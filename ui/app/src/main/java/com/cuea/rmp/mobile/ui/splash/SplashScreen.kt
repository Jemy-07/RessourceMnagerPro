package com.cuea.rmp.mobile.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cuea.rmp.mobile.ui.session.SessionViewModel

/** Branding placeholder + one-shot auth check; swaps for a polished splash in a later sprint. */
@Composable
fun SplashScreen(
    sessionViewModel: SessionViewModel,
    onFinished: (authenticated: Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        onFinished(sessionViewModel.awaitAuthenticationState())
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Resource Manager Pro", style = MaterialTheme.typography.headlineSmall)
    }
}
