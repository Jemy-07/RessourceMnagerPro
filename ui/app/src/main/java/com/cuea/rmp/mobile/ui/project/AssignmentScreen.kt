package com.cuea.rmp.mobile.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

/**
 * List + tap-to-edit form. Drag-and-drop reordering/reassignment is a stretch goal that
 * didn't make this sprint — flagged as a follow-up rather than slipping the whole screen.
 */
@Composable
fun AssignmentScreen(viewModel: AssignmentViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Assignments", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
                    Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh")
                }
            }
        }

        uiState.errorMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error) }
        }

        items(uiState.assignments, key = { it.id }) { item ->
            AssignmentCard(item = item, onClick = { viewModel.startEdit(item) })
        }

        item { HorizontalDivider() }

        item {
            AssignmentForm(uiState = uiState, viewModel = viewModel)
        }
    }
}

@Composable
private fun AssignmentCard(item: AssignmentItemUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text("resource: ${item.resourceId}", style = MaterialTheme.typography.bodySmall)
            Text("period: ${item.period}", style = MaterialTheme.typography.bodySmall)
            Text("allocation: ${item.allocationPct}%  status: ${item.status}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AssignmentForm(uiState: AssignmentUiState, viewModel: AssignmentViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (uiState.editingId == null) "New assignment" else "Edit assignment",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChanged,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.resourceId,
            onValueChange = viewModel::onResourceIdChanged,
            label = { Text("Resource ID (UUID)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.startDate,
            onValueChange = viewModel::onStartDateChanged,
            label = { Text("Start date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.endDate,
            onValueChange = viewModel::onEndDateChanged,
            label = { Text("End date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.allocationPct,
            onValueChange = viewModel::onAllocationPctChanged,
            label = { Text("Allocation %") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::submit, enabled = !uiState.isSaving) {
                Text(if (uiState.isSaving) "Saving..." else if (uiState.editingId == null) "Create" else "Save")
            }
            if (uiState.editingId != null) {
                OutlinedButton(onClick = viewModel::startCreate) { Text("Cancel edit") }
            }
        }

        HorizontalDivider()

        SkillsMatchSection(uiState = uiState, viewModel = viewModel)
    }
}

@Composable
private fun SkillsMatchSection(uiState: AssignmentUiState, viewModel: AssignmentViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Find available resource by skill", style = MaterialTheme.typography.titleSmall)

        OutlinedTextField(
            value = uiState.matchSkillId,
            onValueChange = viewModel::onMatchSkillIdChanged,
            label = { Text("Skill ID (UUID)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.matchFrom,
                onValueChange = viewModel::onMatchFromChanged,
                label = { Text("From (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.matchTo,
                onValueChange = viewModel::onMatchToChanged,
                label = { Text("To (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedButton(onClick = viewModel::searchMatches, enabled = !uiState.isMatching) {
            Text(if (uiState.isMatching) "Searching..." else "Find matching resources")
        }

        uiState.matchError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        uiState.matchResults.forEach { match ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                onClick = { viewModel.selectMatch(match.resourceId) }
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(match.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "type: ${match.type}  proficiency: ${match.proficiency}  rate: ${match.rate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("Tap to use this resource", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
