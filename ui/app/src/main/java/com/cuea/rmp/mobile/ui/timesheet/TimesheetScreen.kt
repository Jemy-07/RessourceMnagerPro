package com.cuea.rmp.mobile.ui.timesheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimesheetScreen(
    viewModel: TimesheetViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Timesheet Log", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onLogout) { Text("Logout") }
        }

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

        Spacer(modifier = Modifier.height(8.dp))
        Text("Local entries", style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.entries, key = { it.id }) { entry ->
                TimesheetEntryCard(entry = entry)
            }
        }
    }
}

@Composable
private fun TimesheetEntryCard(entry: TimesheetEntryUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text("${entry.workDate}  ${entry.hours}h", style = MaterialTheme.typography.bodyLarge)
        Text("resource: ${entry.resourceId}", style = MaterialTheme.typography.bodySmall)
        Text("assignment: ${entry.assignmentId}", style = MaterialTheme.typography.bodySmall)
        Text("status: ${entry.syncState}", style = MaterialTheme.typography.bodySmall)
    }
}


