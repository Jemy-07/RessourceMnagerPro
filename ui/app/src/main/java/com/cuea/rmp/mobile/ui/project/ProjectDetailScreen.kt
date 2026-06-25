package com.cuea.rmp.mobile.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
