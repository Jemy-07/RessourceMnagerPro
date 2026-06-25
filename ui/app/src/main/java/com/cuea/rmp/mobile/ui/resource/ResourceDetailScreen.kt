package com.cuea.rmp.mobile.ui.resource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun ResourceDetailScreen(viewModel: ResourceDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Resource detail", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
                Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh")
            }
        }

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        uiState.resource?.let { resource ->
            Text(resource.name, style = MaterialTheme.typography.titleLarge)
            Text("type: ${resource.type}", style = MaterialTheme.typography.bodyMedium)
            Text("rate: ${resource.rate}", style = MaterialTheme.typography.bodyMedium)
            Text("availability status: ${resource.availabilityStatus}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "skills: ${resource.skillsSummary.ifBlank { "none recorded" }}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        HorizontalDivider()

        Text("Check availability for a date range", style = MaterialTheme.typography.titleMedium)
        // No backend endpoint lists this resource's time-off directly — this is the
        // closest available substitute (server-computed: status + assignment overlap +
        // time-off overlap, via resource/domain/AvailabilityChecker.java).
        Text(
            "(No backend endpoint exposes a time-off list directly — this checks availability " +
                "for a range, which factors in time-off internally.)",
            style = MaterialTheme.typography.labelSmall
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.availabilityFrom,
                onValueChange = viewModel::onAvailabilityFromChanged,
                label = { Text("From (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.availabilityTo,
                onValueChange = viewModel::onAvailabilityToChanged,
                label = { Text("To (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedButton(onClick = viewModel::checkAvailability, enabled = !uiState.isCheckingAvailability) {
            Text(if (uiState.isCheckingAvailability) "Checking..." else "Check availability")
        }

        uiState.availabilityError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        uiState.availabilityResult?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}
