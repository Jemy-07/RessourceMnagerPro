package com.cuea.rmp.mobile.ui.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationScreen(viewModel: NotificationViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Notifications", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
                Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh")
            }
        }

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.notifications, key = { it.id }) { item ->
                NotificationCard(item = item, onMarkRead = { viewModel.markRead(item.id) })
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItemUi,
    onMarkRead: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(item.type, style = MaterialTheme.typography.labelMedium)
        Text(item.message, style = MaterialTheme.typography.bodyMedium)
        if (!item.read) {
            Button(onClick = onMarkRead, modifier = Modifier.padding(top = 6.dp)) {
                Text("Mark read")
            }
        } else {
            Text("Read", style = MaterialTheme.typography.bodySmall)
        }
    }
}

