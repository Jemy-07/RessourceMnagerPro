package com.cuea.rmp.mobile.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cuea.rmp.mobile.sync.SyncFailureUi

/**
 * Makes a failed sync visible instead of silently discarded (Cleanup Half-Sprint) — a
 * permanent failure (4xx, e.g. a rejected value) needs the user to fix something; a
 * transient one is already being retried automatically. The backend doesn't give a
 * finer-grained signal than that split, so the wording stays generic rather than
 * guessing at specifics it can't back up.
 */
@Composable
fun SyncFailureCard(failure: SyncFailureUi, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (failure.isPermanent) "Sync failed — fix and retry" else "Sync failed — will retry automatically",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                failure.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            OutlinedButton(onClick = onRetry) {
                Text("Retry now")
            }
        }
    }
}
