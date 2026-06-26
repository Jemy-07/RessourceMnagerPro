package com.cuea.rmp.mobile.ui.resource

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
            if (uiState.pendingEdit) {
                Text(
                    "Edit pending sync — will push next time the device is online.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (uiState.conflicts.isNotEmpty()) {
            ConflictsCard(uiState.conflicts)
        }

        // ResourceController's update/addSkill endpoints are ADMIN/MANAGER-only server-side
        // (Sprint 3.5 RBAC audit) — hidden rather than shown-then-403'd for other roles.
        if (uiState.canEdit) {
            HorizontalDivider()
            if (uiState.isEditing) {
                ResourceEditForm(uiState = uiState, viewModel = viewModel)
            } else {
                OutlinedButton(onClick = viewModel::startEdit, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit resource")
                }
            }
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.availabilityFrom,
                onValueChange = viewModel::onAvailabilityFromChanged,
                label = { Text("From (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.availabilityTo,
                onValueChange = viewModel::onAvailabilityToChanged,
                label = { Text("To (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedButton(onClick = viewModel::checkAvailability, enabled = !uiState.isCheckingAvailability) {
            Text(if (uiState.isCheckingAvailability) "Checking..." else "Check availability")
        }

        uiState.availabilityError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        uiState.availabilityResult?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun ResourceEditForm(uiState: ResourceDetailUiState, viewModel: ResourceDetailViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Edit resource", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = uiState.editName,
            onValueChange = viewModel::onEditNameChanged,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.editRateAmount,
                onValueChange = viewModel::onEditRateAmountChanged,
                label = { Text("Hourly rate") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.editCurrency,
                onValueChange = viewModel::onEditCurrencyChanged,
                label = { Text("Currency") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = uiState.editAvailabilityStatus,
            onValueChange = viewModel::onEditAvailabilityStatusChanged,
            label = { Text("Availability status") },
            singleLine = true,
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
private fun ConflictsCard(conflicts: List<ConflictUi>) {
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
