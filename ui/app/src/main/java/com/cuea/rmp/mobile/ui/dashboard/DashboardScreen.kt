package com.cuea.rmp.mobile.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuea.rmp.mobile.project.AssignmentLocalEntity
import com.cuea.rmp.mobile.reporting.dto.UtilizationRowResponse
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onViewAllAlerts: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = viewModel::refreshReports, enabled = !uiState.isLoadingReports) {
                    Text(if (uiState.isLoadingReports) "Loading..." else "Refresh")
                }
            }
        }

        item { CostSummaryCard(uiState) }
        item { UtilizationCard(uiState) }
        item { UpcomingAssignmentsCard(uiState.upcomingAssignments) }
        item { AlertsCard(unreadCount = uiState.unreadAlertCount, onViewAll = onViewAllAlerts) }
    }
}

@Composable
private fun CostSummaryCard(uiState: DashboardUiState) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Cost summary", style = MaterialTheme.typography.titleMedium)
            when {
                uiState.reportsForbidden -> Text(
                    uiState.reportsErrorMessage ?: "Not available for your role.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                uiState.reportsErrorMessage != null -> Text(
                    uiState.reportsErrorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                uiState.costSummary == null -> Text(
                    if (uiState.isLoadingReports) "Loading..." else "No cost data yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
                else -> {
                    val summary = uiState.costSummary
                    Text("${summary.projectCount} project(s)", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Allocated: ${"%.2f".format(summary.totalAllocated)} ${summary.currency}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Spent: ${"%.2f".format(summary.totalSpent)} ${summary.currency}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Margin: ${"%.2f".format(summary.totalMargin)} ${summary.currency}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun UtilizationCard(uiState: DashboardUiState) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Utilization", style = MaterialTheme.typography.titleMedium)
            when {
                uiState.reportsForbidden -> Text(
                    uiState.reportsErrorMessage ?: "Not available for your role.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                uiState.reportsErrorMessage != null -> Text(
                    uiState.reportsErrorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                uiState.utilizationRows.isEmpty() -> Text(
                    if (uiState.isLoadingReports) "Loading..." else "No utilization data yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
                else -> UtilizationChart(uiState.utilizationRows)
            }
        }
    }
}

@Composable
private fun UtilizationChart(rows: List<UtilizationRowResponse>) {
    // A column chart of allocated % per resource — a reasonable v1 substitute for a
    // true heat-map grid, which is over-engineering for this sprint's scope.
    val producer = remember(rows) {
        ChartEntryModelProducer(
            listOf(rows.mapIndexed { index, row -> entryOf(index.toFloat(), row.allocatedPct.toFloat()) })
        )
    }
    val labels = rows.map { it.resourceName }
    val bottomFormatter = remember(labels) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            labels.getOrNull(value.toInt()).orEmpty()
        }
    }

    ProvideChartStyle(m3ChartStyle()) {
        Chart(
            chart = columnChart(),
            chartModelProducer = producer,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(valueFormatter = bottomFormatter),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        rows.forEach { row ->
            Text(
                "${row.resourceName}: ${row.allocatedPct}% allocated, ${row.activeAssignments} active (${row.level})",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun UpcomingAssignmentsCard(assignments: List<AssignmentLocalEntity>) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Upcoming assignments", style = MaterialTheme.typography.titleMedium)
            if (assignments.isEmpty()) {
                Text(
                    "No upcoming assignments cached yet. Open a project's assignments to " +
                        "populate this (there's no backend \"list all assignments\" endpoint).",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                assignments.forEach { assignment ->
                    Text(
                        "${assignment.title} — ${assignment.startDate} to ${assignment.endDate} " +
                            "(${assignment.allocationPct}%)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsCard(unreadCount: Int, onViewAll: () -> Unit) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Alerts", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onViewAll) { Text("View all") }
            }
            Text(
                if (unreadCount == 0) "No unread notifications." else "$unreadCount unread notification(s).",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
