package com.cuea.rmp.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cuea.rmp.mobile.ui.auth.AuthScreen
import com.cuea.rmp.mobile.ui.auth.AuthViewModel
import com.cuea.rmp.mobile.ui.budget.BudgetScreen
import com.cuea.rmp.mobile.ui.dashboard.DashboardScreen
import com.cuea.rmp.mobile.ui.navigation.Routes
import com.cuea.rmp.mobile.ui.navigation.bottomNavDestinations
import com.cuea.rmp.mobile.ui.notification.NotificationScreen
import com.cuea.rmp.mobile.ui.notification.NotificationViewModel
import com.cuea.rmp.mobile.ui.profile.ProfileScreen
import com.cuea.rmp.mobile.ui.project.AssignmentScreen
import com.cuea.rmp.mobile.ui.project.ProjectDetailScreen
import com.cuea.rmp.mobile.ui.project.ProjectScreen
import com.cuea.rmp.mobile.ui.project.ProjectViewModel
import com.cuea.rmp.mobile.ui.reporting.ReportScreen
import com.cuea.rmp.mobile.ui.request.RequestScreen
import com.cuea.rmp.mobile.ui.request.RequestViewModel
import com.cuea.rmp.mobile.ui.resource.ResourceDetailScreen
import com.cuea.rmp.mobile.ui.resource.ResourceScreen
import com.cuea.rmp.mobile.ui.resource.ResourceViewModel
import com.cuea.rmp.mobile.ui.session.SessionViewModel
import com.cuea.rmp.mobile.ui.splash.SplashScreen
import com.cuea.rmp.mobile.ui.timesheet.TimesheetScreen
import com.cuea.rmp.mobile.ui.timesheet.TimesheetViewModel

/** Routes that show the shared top bar + bottom nav chrome (everything but Splash/Auth). */
private val chromeRoutes = setOf(
    Routes.Dashboard.route,
    Routes.ResourceList.route,
    Routes.ResourceCalendar.route,
    Routes.ProjectList.route,
    Routes.ProjectDetail.route,
    Routes.TaskAssignmentManagement.route,
    Routes.TimesheetEntry.route,
    Routes.WhatIfScenarioPlanner.route,
    Routes.BudgetOverview.route,
    Routes.Reports.route,
    Routes.NotificationsCenter.route,
    Routes.Settings.route,
    Routes.Search.route,
    Routes.Profile.route,
    Routes.Help.route,
    Routes.Requests.route
)

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
    val navController = rememberNavController()
    val isAuthenticated by sessionViewModel.isAuthenticated.collectAsState()
    var splashResolved by remember { mutableStateOf(false) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showChrome = currentRoute in chromeRoutes

    // Keeps login/logout working exactly as before, now expressed as nav transitions
    // instead of swapping a top-level `if`. Only acts once the splash's one-shot check
    // has resolved, so it never fights the initial Splash -> Auth/Dashboard navigation.
    LaunchedEffect(isAuthenticated, splashResolved, currentRoute) {
        if (!splashResolved) return@LaunchedEffect
        if (isAuthenticated && currentRoute == Routes.Auth.route) {
            navController.navigate(Routes.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        } else if (!isAuthenticated && currentRoute != Routes.Auth.route && currentRoute != Routes.Splash.route) {
            navController.navigate(Routes.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            if (showChrome) {
                AppTopBar(
                    authViewModel = authViewModel,
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        },
        bottomBar = {
            if (showChrome) {
                AppBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Splash.route,
            modifier = Modifier.padding(contentPadding)
        ) {
            composable(Routes.Splash.route) {
                SplashScreen(
                    sessionViewModel = sessionViewModel,
                    onFinished = { authenticated ->
                        splashResolved = true
                        val target = if (authenticated) Routes.Dashboard.route else Routes.Auth.route
                        navController.navigate(target) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.Auth.route) { AuthScreen(viewModel = authViewModel) }

            composable(Routes.Dashboard.route) {
                DashboardScreen(
                    onViewAllAlerts = { navController.navigate(Routes.NotificationsCenter.route) }
                )
            }
            composable(Routes.ResourceList.route) {
                ResourceScreen(
                    viewModel = resourceViewModel,
                    onResourceClick = { resourceId ->
                        navController.navigate(Routes.ResourceDetail.createRoute(resourceId))
                    }
                )
            }
            composable(
                Routes.ResourceDetail.route,
                arguments = listOf(navArgument("resourceId") { type = NavType.StringType })
            ) {
                ResourceDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ResourceCalendar.route) { PlaceholderScreen("Resource Calendar") }
            composable(Routes.ProjectList.route) {
                ProjectScreen(
                    viewModel = projectViewModel,
                    onProjectClick = { projectId ->
                        navController.navigate(Routes.ProjectDetail.createRoute(projectId))
                    }
                )
            }
            composable(
                Routes.ProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) {
                ProjectDetailScreen(
                    onManageAssignments = { projectId ->
                        navController.navigate(Routes.TaskAssignmentManagement.createRoute(projectId))
                    },
                    onViewBudget = { projectId ->
                        navController.navigate(Routes.BudgetOverview.createRoute(projectId))
                    }
                )
            }
            composable(
                Routes.TaskAssignmentManagement.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) {
                AssignmentScreen()
            }
            composable(Routes.TimesheetEntry.route) { TimesheetScreen(viewModel = timesheetViewModel) }
            composable(Routes.WhatIfScenarioPlanner.route) { PlaceholderScreen("What-If Scenario Planner") }
            composable(
                Routes.BudgetOverview.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) {
                BudgetScreen()
            }
            composable(Routes.Reports.route) { ReportScreen() }
            composable(Routes.NotificationsCenter.route) { NotificationScreen(viewModel = notificationViewModel) }
            composable(Routes.Settings.route) { PlaceholderScreen("Settings") }
            composable(Routes.Search.route) { PlaceholderScreen("Search") }
            composable(Routes.Profile.route) { ProfileScreen() }
            composable(Routes.Help.route) { PlaceholderScreen("Help") }
            composable(Routes.Requests.route) { RequestScreen(viewModel = requestViewModel) }
        }
    }
}

// Routes that are "top-level" — no back arrow needed in the top bar.
private val primaryRoutes = setOf(
    Routes.Dashboard.route,
    Routes.ResourceList.route,
    Routes.ProjectList.route,
    Routes.ResourceCalendar.route,
    Routes.Reports.route
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    currentRoute: String?
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val showBack = currentRoute != null && currentRoute !in primaryRoutes

    TopAppBar(
        title = { Text("Resource Manager Pro") },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Timesheets") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Routes.TimesheetEntry.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Notifications") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Routes.NotificationsCenter.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Requests") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Routes.Requests.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Profile") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Routes.Profile.route)
                    }
                )
            }
            IconButton(onClick = { authViewModel.logout() }) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            }
        }
    )
}

@Composable
private fun AppBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        bottomNavDestinations.forEach { destination ->
            val selected = currentRoute == destination.routes.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.routes.route) {
                        popUpTo(Routes.Dashboard.route) {
                            saveState = true
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}
