package com.cuea.rmp.mobile.ui.timesheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cuea.rmp.mobile.ui.common.SyncFailureCard

@Composable
fun TimesheetScreen(viewModel: TimesheetViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Timesheet Log", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = uiState.resourceId,
            onValueChange = viewModel::onResourceIdChanged,
            label = { Text("Resource ID (UUID)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.assignmentId,
            onValueChange = viewModel::onAssignmentIdChanged,
            label = { Text("Assignment ID (UUID)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.workDate,
            onValueChange = viewModel::onWorkDateChanged,
            label = { Text("Work date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.hours,
            onValueChange = viewModel::onHoursChanged,
            label = { Text("Hours (0-24)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = viewModel::submit,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isLoading) "Saving..." else "Log timesheet")
        }

        OutlinedButton(
            onClick = viewModel::syncNow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sync now")
        }

        uiState.message?.let { message ->
            Text(text = message)
        }

        Text("Local entries", style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.entries, key = { it.id }) { entry ->
                TimesheetEntryCard(entry = entry, onRetrySync = { viewModel.retrySync(entry.id) })
            }
        }
    }
}

@Composable
private fun TimesheetEntryCard(entry: TimesheetEntryUi, onRetrySync: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text("${entry.workDate}  ${entry.hours}h", style = MaterialTheme.typography.bodyLarge)
        Text("resource: ${entry.resourceId}", style = MaterialTheme.typography.bodySmall)
        Text("assignment: ${entry.assignmentId}", style = MaterialTheme.typography.bodySmall)
        Text("status: ${entry.syncState}", style = MaterialTheme.typography.bodySmall)
        // syncState alone only ever showed the bare word "FAILED" with no explanation —
        // this adds the actual backend message and a way to act on it (Cleanup Half-Sprint).
        entry.syncFailure?.let { failure ->
            SyncFailureCard(failure = failure, onRetry = onRetrySync, modifier = Modifier.fillMaxWidth())
        }
    }
}


