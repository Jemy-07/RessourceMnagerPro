package com.cuea.rmp.mobile.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProjectScreen(viewModel: ProjectViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
            Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh projects")
        }

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.projects, key = { it.id }) { item ->
                ProjectCard(item)
            }
        }
    }
}

@Composable
private fun ProjectCard(item: ProjectItemUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(item.name, style = MaterialTheme.typography.titleMedium)
        Text("status: ${item.status}", style = MaterialTheme.typography.bodySmall)
        Text("manager: ${item.managerId}", style = MaterialTheme.typography.bodySmall)
        Text("period: ${item.period}", style = MaterialTheme.typography.bodySmall)
        if (item.description.isNotBlank()) {
            Text(item.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

