package com.cuea.rmp.mobile.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuea.rmp.mobile.sync.ConflictUi
import com.cuea.rmp.mobile.ui.common.EnumDropdownField
import com.cuea.rmp.mobile.ui.common.SyncFailureCard

private val PROJECT_STATUS_OPTIONS = listOf("PLANNED", "ACTIVE", "ON_HOLD", "DONE")

@Composable
fun ProjectDetailScreen(
    onManageAssignments: (projectId: String) -> Unit,
    onViewBudget: (projectId: String) -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Project detail", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
                Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh")
            }
        }

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        uiState.project?.let { project ->
            Text(project.name, style = MaterialTheme.typography.titleLarge)
            Text("status: ${project.status}", style = MaterialTheme.typography.bodyMedium)
            Text("manager: ${project.managerId}", style = MaterialTheme.typography.bodyMedium)
            Text("period: ${project.period}", style = MaterialTheme.typography.bodyMedium)
            if (project.description.isNotBlank()) {
                Text(project.description, style = MaterialTheme.typography.bodyMedium)
            }
            if (uiState.pendingEdit) {
                Text(
                    "Edit pending sync — will push next time the device is online.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (uiState.conflicts.isNotEmpty()) {
            ProjectConflictsCard(uiState.conflicts)
        }

        uiState.syncFailure?.let { failure ->
            SyncFailureCard(failure = failure, onRetry = viewModel::retrySync, modifier = Modifier.fillMaxWidth())
        }

        // ProjectController's update endpoint is ADMIN/MANAGER-only server-side (Sprint
        // 3.5 RBAC audit) — hidden rather than shown-then-403'd for other roles.
        if (uiState.canEdit) {
            HorizontalDivider()
            if (uiState.isEditing) {
                ProjectEditForm(uiState = uiState, viewModel = viewModel)
            } else {
                OutlinedButton(onClick = viewModel::startEdit, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit project")
                }
            }
        }

        HorizontalDivider()

        Text("${uiState.assignmentCount} assignment(s) on this project", style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = { onManageAssignments(viewModel.projectId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage assignments")
        }

        OutlinedButton(
            onClick = { onViewBudget(viewModel.projectId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View budget")
        }
    }
}

@Composable
private fun ProjectEditForm(uiState: ProjectDetailUiState, viewModel: ProjectDetailViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Edit project", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = uiState.editName,
            onValueChange = viewModel::onEditNameChanged,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.editDescription,
            onValueChange = viewModel::onEditDescriptionChanged,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.editStartDate,
                onValueChange = viewModel::onEditStartDateChanged,
                label = { Text("Start date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.editEndDate,
                onValueChange = viewModel::onEditEndDateChanged,
                label = { Text("End date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        EnumDropdownField(
            label = "Status",
            options = PROJECT_STATUS_OPTIONS,
            selected = uiState.editStatus,
            onSelected = viewModel::onEditStatusChanged,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.editError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::saveEdit, enabled = !uiState.isSaving) {
                Text(if (uiState.isSaving) "Saving..." else "Save")
            }
            OutlinedButton(onClick = viewModel::cancelEdit) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ProjectConflictsCard(conflicts: List<ConflictUi>) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Sync conflicts", style = MaterialTheme.typography.titleMedium)
            conflicts.forEach { conflict ->
                val outcome = if (conflict.resolution == "CLIENT_WON") {
                    "Your edit was applied"
                } else {
                    "Your edit was discarded — someone else's change won"
                }
                Text(outcome, style = MaterialTheme.typography.bodyMedium)
                Text(conflict.message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
