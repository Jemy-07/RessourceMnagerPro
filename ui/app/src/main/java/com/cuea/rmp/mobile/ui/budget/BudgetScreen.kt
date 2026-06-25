package com.cuea.rmp.mobile.ui.budget

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BudgetScreen(viewModel: BudgetViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Budget overview", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
                Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh")
            }
        }

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        val budget = uiState.budget
        if (budget != null) {
            // Values are displayed exactly as returned by the backend (Budget.margin()/
            // remaining() are real server-side calculations) — never recomputed here.
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BudgetRow("Total", budget.totalAmount, budget.currency)
                BudgetRow("Allocated", budget.allocatedAmount, budget.currency)
                BudgetRow("Spent", budget.spentAmount, budget.currency)
                BudgetRow("Margin", budget.margin, budget.currency)
                BudgetRow("Remaining", budget.remaining, budget.currency)
            }
        } else if (!uiState.isRefreshing) {
            Text("No budget allocated for this project yet.", style = MaterialTheme.typography.bodyMedium)
        }

        HorizontalDivider()

        Text("Allocate budget", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = uiState.allocationTotal,
            onValueChange = viewModel::onAllocationTotalChanged,
            label = { Text("Total amount") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.allocationAllocated,
            onValueChange = viewModel::onAllocationAllocatedChanged,
            label = { Text("Allocated amount") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.allocationCurrency,
            onValueChange = viewModel::onAllocationCurrencyChanged,
            label = { Text("Currency (e.g. USD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = viewModel::submitAllocation,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isSaving) "Saving..." else "Save allocation")
        }
    }
}

@Composable
private fun BudgetRow(label: String, amount: Double, currency: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text("$amount $currency", style = MaterialTheme.typography.bodyLarge)
    }
}
