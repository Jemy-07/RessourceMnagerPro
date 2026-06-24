package com.cuea.rmp.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cuea.rmp.mobile.ui.AppRoot
import com.cuea.rmp.mobile.ui.auth.AuthViewModel
import com.cuea.rmp.mobile.ui.session.SessionViewModel
import com.cuea.rmp.mobile.ui.theme.RmpMobileTheme
import com.cuea.rmp.mobile.ui.timesheet.TimesheetViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sessionViewModel: SessionViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val timesheetViewModel: TimesheetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RmpMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(
                        sessionViewModel = sessionViewModel,
                        authViewModel = authViewModel,
                        timesheetViewModel = timesheetViewModel
                    )
                }
            }
        }
    }
}

