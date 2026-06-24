package com.cuea.rmp.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cuea.rmp.mobile.ui.auth.AuthScreen
import com.cuea.rmp.mobile.ui.auth.AuthViewModel
import com.cuea.rmp.mobile.ui.notification.NotificationScreen
import com.cuea.rmp.mobile.ui.notification.NotificationViewModel
import com.cuea.rmp.mobile.ui.project.ProjectScreen
import com.cuea.rmp.mobile.ui.project.ProjectViewModel
import com.cuea.rmp.mobile.ui.request.RequestScreen
import com.cuea.rmp.mobile.ui.request.RequestViewModel
import com.cuea.rmp.mobile.ui.resource.ResourceScreen
import com.cuea.rmp.mobile.ui.resource.ResourceViewModel
import com.cuea.rmp.mobile.ui.session.SessionViewModel
import com.cuea.rmp.mobile.ui.timesheet.TimesheetScreen
import com.cuea.rmp.mobile.ui.timesheet.TimesheetViewModel

@Composable
fun AppRoot(
    sessionViewModel: SessionViewModel,
    authViewModel: AuthViewModel,
    timesheetViewModel: TimesheetViewModel,
    notificationViewModel: NotificationViewModel,
    resourceViewModel: ResourceViewModel,
    projectViewModel: ProjectViewModel,
    requestViewModel: RequestViewModel
) {
    val isAuthenticated by sessionViewModel.isAuthenticated.collectAsState()
    var selectedTab by remember { mutableStateOf(AuthenticatedTab.TIMESHEETS) }
    val tabScroll = rememberScrollState()

    if (isAuthenticated) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Resource Manager Pro",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { authViewModel.logout() }) {
                    Text("Logout")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tabScroll)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AuthenticatedTab.entries.forEach { tab ->
                    TextButton(onClick = { selectedTab = tab }) {
                        val isSelected = selectedTab == tab
                        Text(
                            text = tab.label,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            when (selectedTab) {
                AuthenticatedTab.TIMESHEETS -> TimesheetScreen(viewModel = timesheetViewModel)

                AuthenticatedTab.NOTIFICATIONS -> NotificationScreen(viewModel = notificationViewModel)

                AuthenticatedTab.RESOURCES -> ResourceScreen(viewModel = resourceViewModel)

                AuthenticatedTab.PROJECTS -> ProjectScreen(viewModel = projectViewModel)

                AuthenticatedTab.REQUESTS -> RequestScreen(viewModel = requestViewModel)
            }
        }
    } else {
        AuthScreen(viewModel = authViewModel)
    }
}

private enum class AuthenticatedTab {
    TIMESHEETS,
    NOTIFICATIONS,
    RESOURCES,
    PROJECTS,
    REQUESTS;

    val label: String
        get() = when (this) {
            TIMESHEETS -> "Timesheets"
            NOTIFICATIONS -> "Notifications"
            RESOURCES -> "Resources"
            PROJECTS -> "Projects"
            REQUESTS -> "Requests"
        }
}


