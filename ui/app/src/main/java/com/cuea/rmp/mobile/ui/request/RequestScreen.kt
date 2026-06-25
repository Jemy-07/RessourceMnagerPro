package com.cuea.rmp.mobile.ui.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RequestScreen(viewModel: RequestViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
            Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh requests")
        }

        OutlinedTextField(
            value = uiState.rejectComment,
            onValueChange = viewModel::onRejectCommentChanged,
            label = { Text("Reject comment") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.requests, key = { it.id }) { item ->
                RequestCard(
                    item = item,
                    onApprove = { viewModel.approve(item.id) },
                    onReject = { viewModel.reject(item.id) }
                )
            }
        }
    }
}

@Composable
private fun RequestCard(
    item: RequestItemUi,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(item.title, style = MaterialTheme.typography.titleMedium)
        Text("status: ${item.status}", style = MaterialTheme.typography.bodySmall)
        Text("resource: ${item.resourceId}", style = MaterialTheme.typography.bodySmall)
        Text("project: ${item.projectId}", style = MaterialTheme.typography.bodySmall)
        Text("period: ${item.period}", style = MaterialTheme.typography.bodySmall)
        Text("allocation: ${item.allocationPct}%", style = MaterialTheme.typography.bodySmall)
        if (item.comments.isNotBlank()) {
            Text("comments: ${item.comments}", style = MaterialTheme.typography.bodySmall)
        }

        if (item.status == "PENDING") {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove) { Text("Approve") }
                OutlinedButton(onClick = onReject) { Text("Reject") }
            }
        }
    }
}

