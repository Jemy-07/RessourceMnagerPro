package com.cuea.rmp.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.cuea.rmp.mobile.ui.auth.AuthScreen
import com.cuea.rmp.mobile.ui.auth.AuthViewModel
import com.cuea.rmp.mobile.ui.session.SessionViewModel
import com.cuea.rmp.mobile.ui.timesheet.TimesheetScreen
import com.cuea.rmp.mobile.ui.timesheet.TimesheetViewModel

@Composable
fun AppRoot(
    sessionViewModel: SessionViewModel,
    authViewModel: AuthViewModel,
    timesheetViewModel: TimesheetViewModel
) {
    val isAuthenticated by sessionViewModel.isAuthenticated.collectAsState()

    if (isAuthenticated) {
        TimesheetScreen(
            viewModel = timesheetViewModel,
            onLogout = { authViewModel.logout() }
        )
    } else {
        AuthScreen(viewModel = authViewModel)
    }
}


