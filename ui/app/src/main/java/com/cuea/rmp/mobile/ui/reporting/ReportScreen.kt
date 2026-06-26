package com.cuea.rmp.mobile.ui.reporting

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuea.rmp.mobile.reporting.ReportExportType
import com.cuea.rmp.mobile.reporting.dto.CostRowResponse
import com.cuea.rmp.mobile.reporting.dto.SkillsGapRowResponse
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Reports", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isLoading) {
                    Text(if (uiState.isLoading) "Loading..." else "Refresh")
                }
            }
        }

        uiState.errorMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error) }
        }

        uiState.exportMessage?.let { message ->
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(message, style = MaterialTheme.typography.bodySmall)
                    if (uiState.lastExportedUri != null) {
                        OutlinedButton(onClick = {
                            val uri = uiState.lastExportedUri ?: return@OutlinedButton
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Open PDF")
                        }
                    }
                }
            }
        }

        if (!uiState.isForbidden) {
            item {
                ReportSection(
                    title = "Cost report",
                    isExporting = uiState.exportingType == ReportExportType.COST,
                    onExport = { viewModel.exportPdf(ReportExportType.COST) }
                ) {
                    CostReportChart(uiState.costRows)
                }
            }

            item {
                ReportSection(
                    title = "Skills gap report",
                    isExporting = uiState.exportingType == ReportExportType.SKILLS_GAP,
                    onExport = { viewModel.exportPdf(ReportExportType.SKILLS_GAP) }
                ) {
                    SkillsGapReportChart(uiState.skillsGapRows)
                }
            }
        }

        item { HorizontalDivider() }

        item { ComingSoonCard("Availability Forecast") }
        item { ComingSoonCard("Allocation Summary") }
        item { ComingSoonCard("Timesheet Compliance") }
    }
}

@Composable
private fun ReportSection(
    title: String,
    isExporting: Boolean,
    onExport: () -> Unit,
    content: @Composable () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onExport, enabled = !isExporting) {
                    Text(if (isExporting) "Exporting..." else "Export PDF")
                }
            }
            content()
        }
    }
}

@Composable
private fun CostReportChart(rows: List<CostRowResponse>) {
    if (rows.isEmpty()) {
        Text("No project cost data yet.", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val producer = remember(rows) {
        ChartEntryModelProducer(
            listOf(
                rows.mapIndexed { index, row -> entryOf(index.toFloat(), row.allocatedAmount.toFloat()) },
                rows.mapIndexed { index, row -> entryOf(index.toFloat(), row.spentAmount.toFloat()) }
            )
        )
    }
    val labels = rows.map { it.projectName }
    val bottomFormatter = remember(labels) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            labels.getOrNull(value.toInt()).orEmpty()
        }
    }

    ProvideChartStyle(m3ChartStyle()) {
        Chart(
            chart = columnChart(mergeMode = ColumnChart.MergeMode.Grouped),
            chartModelProducer = producer,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(valueFormatter = bottomFormatter),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
    Text("Allocated vs. spent, by project", style = MaterialTheme.typography.labelSmall)

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        rows.forEach { row ->
            Text(
                "${row.projectName}: margin ${"%.2f".format(row.margin)} ${row.currency} " +
                    "(${"%.1f".format(row.marginPct)}%)",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SkillsGapReportChart(rows: List<SkillsGapRowResponse>) {
    if (rows.isEmpty()) {
        Text("No skills data yet.", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val producer = remember(rows) {
        ChartEntryModelProducer(
            listOf(rows.mapIndexed { index, row -> entryOf(index.toFloat(), row.resourceCount.toFloat()) })
        )
    }
    val labels = rows.map { it.skillName }
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
                .height(220.dp)
        )
    }
    Text("Resources covering each skill", style = MaterialTheme.typography.labelSmall)

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        rows.forEach { row ->
            Text(
                "${row.skillName}: ${row.resourceCount} resource(s), avg proficiency " +
                    "${"%.1f".format(row.avgProficiency)}" + if (row.gap) "  ⚠ gap" else "",
                style = MaterialTheme.typography.bodySmall,
                color = if (row.gap) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ComingSoonCard(title: String) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                "Coming soon — no backend endpoint exists for this report yet " +
                    "(ReportType enum only defines COST, SKILLS_GAP, UTILIZATION).",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
