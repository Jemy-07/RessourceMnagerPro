package com.cuea.rmp.mobile.ui.resource

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
fun ResourceScreen(viewModel: ResourceViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
            Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh resources")
        }

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.resources, key = { it.id }) { item ->
                ResourceCard(item)
            }
        }
    }
}

@Composable
private fun ResourceCard(item: ResourceItemUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(item.name, style = MaterialTheme.typography.titleMedium)
        Text("type: ${item.type}", style = MaterialTheme.typography.bodySmall)
        Text("rate: ${item.rate}", style = MaterialTheme.typography.bodySmall)
        Text("availability: ${item.availability}", style = MaterialTheme.typography.bodySmall)
        if (item.skillsSummary.isNotBlank()) {
            Text("skills: ${item.skillsSummary}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

