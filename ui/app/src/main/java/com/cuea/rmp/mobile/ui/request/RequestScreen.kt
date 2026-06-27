package com.cuea.rmp.mobile.ui.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cuea.rmp.mobile.ui.common.SyncFailureCard

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

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        // Visible to every role — RequestController.create is open to any authenticated
        // user (unlike Resource/Project edits, which are ADMIN/MANAGER-only). Works
        // offline: queued locally and replayed once the device is back online.
        NewRequestForm(uiState = uiState, viewModel = viewModel)

        HorizontalDivider()

        OutlinedTextField(
            value = uiState.rejectComment,
            onValueChange = viewModel::onRejectCommentChanged,
            label = { Text("Reject comment") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.requests, key = { it.id }) { item ->
                RequestCard(
                    item = item,
                    onApprove = { viewModel.approve(item.id) },
                    onReject = { viewModel.reject(item.id) },
                    onRetrySync = { viewModel.retrySync(item.id) }
                )
            }
        }
    }
}

@Composable
private fun NewRequestForm(uiState: RequestUiState, viewModel: RequestViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("New request", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.newResourceId,
                onValueChange = viewModel::onNewResourceIdChanged,
                label = { Text("Resource ID (UUID)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.newProjectId,
                onValueChange = viewModel::onNewProjectIdChanged,
                label = { Text("Project ID (UUID)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = uiState.newTitle,
            onValueChange = viewModel::onNewTitleChanged,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.newStartDate,
                onValueChange = viewModel::onNewStartDateChanged,
                label = { Text("Start date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.newEndDate,
                onValueChange = viewModel::onNewEndDateChanged,
                label = { Text("End date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = uiState.newAllocationPct,
            onValueChange = viewModel::onNewAllocationPctChanged,
            label = { Text("Allocation %") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = viewModel::createRequest, enabled = !uiState.isCreating) {
            Text(if (uiState.isCreating) "Creating..." else "Create request")
        }
    }
}

@Composable
private fun RequestCard(
    item: RequestItemUi,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRetrySync: () -> Unit
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

        // This row's "PENDING" looks identical to a normal awaiting-approval request even
        // when it never actually reached the server — the failure card below is what
        // distinguishes the two (used to be indistinguishable, Cleanup Half-Sprint).
        item.syncFailure?.let { failure ->
            SyncFailureCard(failure = failure, onRetry = onRetrySync, modifier = Modifier.fillMaxWidth())
        }

        if (item.status == "PENDING") {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove) { Text("Approve") }
                OutlinedButton(onClick = onReject) { Text("Reject") }
            }
        }
    }
}

