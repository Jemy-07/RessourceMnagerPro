package com.cuea.rmp.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    resourceViewModel: ResourceViewModel
) {
    val isAuthenticated by sessionViewModel.isAuthenticated.collectAsState()
    var selectedTab by remember { mutableStateOf(AuthenticatedTab.TIMESHEETS) }

    if (isAuthenticated) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    TextButton(onClick = { selectedTab = AuthenticatedTab.TIMESHEETS }) {
                        androidx.compose.material3.Text("Timesheets")
                    }
                    TextButton(onClick = { selectedTab = AuthenticatedTab.NOTIFICATIONS }) {
                        androidx.compose.material3.Text("Notifications")
                    }
                    TextButton(onClick = { selectedTab = AuthenticatedTab.RESOURCES }) {
                        androidx.compose.material3.Text("Resources")
                    }
                }
                TextButton(onClick = { authViewModel.logout() }) {
                    androidx.compose.material3.Text("Logout")
                }
            }

            when (selectedTab) {
                AuthenticatedTab.TIMESHEETS -> {
                    TimesheetScreen(
                        viewModel = timesheetViewModel,
                        onLogout = { authViewModel.logout() }
                    )
                }

                AuthenticatedTab.NOTIFICATIONS -> NotificationScreen(viewModel = notificationViewModel)

                AuthenticatedTab.RESOURCES -> ResourceScreen(viewModel = resourceViewModel)
            }
        }
    } else {
        AuthScreen(viewModel = authViewModel)
    }
}

private enum class AuthenticatedTab {
    TIMESHEETS,
    NOTIFICATIONS,
    RESOURCES
}


