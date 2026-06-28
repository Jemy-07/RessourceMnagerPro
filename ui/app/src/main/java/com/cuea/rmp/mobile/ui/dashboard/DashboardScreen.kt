package com.cuea.rmp.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuea.rmp.mobile.project.AssignmentLocalEntity
import com.cuea.rmp.mobile.reporting.dto.UtilizationRowResponse

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onViewAllAlerts: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    uiState.costSummary?.let { s ->
                        Text(
                            "${s.projectCount} project(s) · ${uiState.utilizationRows.size} resources",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = viewModel::refreshReports,
                    enabled = !uiState.isLoadingReports
                ) {
                    if (uiState.isLoadingReports) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }
        }

        item { CostSummarySection(uiState) }
        item { UtilizationCard(uiState) }
        item { UpcomingAssignmentsCard(uiState.upcomingAssignments) }
        item { AlertsCard(unreadCount = uiState.unreadAlertCount, onViewAll = onViewAllAlerts) }
    }
}

// ─── Cost / KPI ─────────────────────────────────────────────────────────────

@Composable
private fun CostSummarySection(uiState: DashboardUiState) {
    when {
        uiState.reportsForbidden ->
            StatusCard(uiState.reportsErrorMessage ?: "Not available for your role.", isError = true)
        uiState.reportsErrorMessage != null ->
            StatusCard(uiState.reportsErrorMessage, isError = true)
        uiState.isLoadingReports && uiState.costSummary == null ->
            StatusCard("Loading cost data…", isError = false)
        uiState.costSummary == null ->
            StatusCard("No cost data yet.", isError = false)
        else -> {
            val s = uiState.costSummary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Allocated",
                    value = "${"%.0f".format(s.totalAllocated)}",
                    unit = s.currency,
                    icon = Icons.Default.AccountBalance,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Spent",
                    value = "${"%.0f".format(s.totalSpent)}",
                    unit = s.currency,
                    icon = Icons.Default.TrendingDown,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Margin",
                    value = "${"%.0f".format(s.totalMargin)}",
                    unit = s.currency,
                    icon = Icons.Default.Savings,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                unit,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

// ─── Utilization ────────────────────────────────────────────────────────────

@Composable
private fun UtilizationCard(uiState: DashboardUiState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Utilization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            when {
                uiState.reportsForbidden || uiState.reportsErrorMessage != null -> Text(
                    uiState.reportsErrorMessage ?: "Not available for your role.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                uiState.utilizationRows.isEmpty() -> Text(
                    if (uiState.isLoadingReports) "Loading…" else "No utilization data yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> {
                    UtilizationSummaryChips(uiState.utilizationRows)
                    UtilizationBarChart(uiState.utilizationRows)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.utilizationRows.forEach { row -> UtilizationRow(row) }
                    }
                }
            }
        }
    }
}

@Composable
private fun UtilizationSummaryChips(rows: List<UtilizationRowResponse>) {
    val avgPct = if (rows.isEmpty()) 0 else rows.sumOf { it.allocatedPct } / rows.size
    val overCount = rows.count { it.level.uppercase() == "OVER" }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(
            text = "${rows.size} resources",
            bg = MaterialTheme.colorScheme.secondaryContainer,
            fg = MaterialTheme.colorScheme.onSecondaryContainer
        )
        StatChip(
            text = "Avg ${avgPct}%",
            bg = MaterialTheme.colorScheme.primaryContainer,
            fg = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (overCount > 0) {
            StatChip(
                text = "$overCount over",
                bg = MaterialTheme.colorScheme.errorContainer,
                fg = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun StatChip(text: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fg
        )
    }
}

@Composable
private fun UtilizationBarChart(rows: List<UtilizationRowResponse>) {
    if (rows.isEmpty()) return
    val maxPct = rows.maxOf { it.allocatedPct }.coerceAtLeast(100)
    val errorColor = MaterialTheme.colorScheme.error
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    fun barColor(level: String) = when (level.uppercase()) {
        "OVER"     -> errorColor
        "HIGH"     -> tertiaryColor
        "MODERATE" -> primaryColor
        else       -> outlineColor
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Bar columns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rows.forEach { row ->
                val fraction = (row.allocatedPct.toFloat() / maxPct).coerceIn(0.02f, 1f)
                val spaceFraction = (1f - fraction).coerceAtLeast(0.02f)
                val color = barColor(row.level)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(color.copy(alpha = 0.12f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().weight(spaceFraction))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(fraction)
                            .background(color)
                    )
                }
            }
        }
        // Percentage labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rows.forEach { row ->
                Text(
                    "${row.allocatedPct}%",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = barColor(row.level),
                    textAlign = TextAlign.Center
                )
            }
        }
        // Resource name labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rows.forEach { row ->
                Text(
                    row.resourceName.split(" ").first().take(7),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariantColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun UtilizationRow(row: UtilizationRowResponse) {
    val levelColor = when (row.level.uppercase()) {
        "OVER"     -> MaterialTheme.colorScheme.error
        "HIGH"     -> MaterialTheme.colorScheme.tertiary
        "MODERATE" -> MaterialTheme.colorScheme.primary
        else       -> MaterialTheme.colorScheme.outline
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    row.resourceName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${row.activeAssignments} active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${row.allocatedPct}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
                LevelBadge(row.level)
            }
        }
        LinearProgressIndicator(
            progress = { (row.allocatedPct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = levelColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun LevelBadge(level: String) {
    val (bg, fg) = when (level.uppercase()) {
        "OVER"     -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "HIGH"     -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "MODERATE" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else       -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            level,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fg
        )
    }
}

// ─── Upcoming Assignments ───────────────────────────────────────────────────

@Composable
private fun UpcomingAssignmentsCard(assignments: List<AssignmentLocalEntity>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Upcoming assignments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (assignments.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "No upcoming assignments cached yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    assignments.forEach { AssignmentRow(it) }
                }
            }
        }
    }
}

@Composable
private fun AssignmentRow(assignment: AssignmentLocalEntity) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    assignment.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${assignment.startDate} → ${assignment.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "${assignment.allocationPct}%",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ─── Alerts ─────────────────────────────────────────────────────────────────

@Composable
private fun AlertsCard(unreadCount: Int, onViewAll: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (unreadCount > 0) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (unreadCount > 0) MaterialTheme.colorScheme.onErrorContainer
                               else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        "Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (unreadCount == 0) "All caught up!" else "$unreadCount unread",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unreadCount > 0) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(
                onClick = onViewAll,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("View all")
            }
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

@Composable
private fun StatusCard(message: String, isError: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isError) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            }
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
