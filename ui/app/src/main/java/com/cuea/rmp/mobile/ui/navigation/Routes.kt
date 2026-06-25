package com.cuea.rmp.mobile.ui.navigation

/**
 * All 14 target screens are declared up front (even where the screen doesn't exist yet)
 * so the nav graph doesn't need rework as later sprints swap placeholders for real screens.
 */
sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Auth : Routes("auth")

    object Dashboard : Routes("dashboard")
    object ResourceList : Routes("resource_list")
    object ResourceDetail : Routes("resource_detail")
    object ResourceCalendar : Routes("resource_calendar")
    object ProjectList : Routes("project_list")
    object ProjectDetail : Routes("project_detail")
    object TaskAssignmentManagement : Routes("task_assignment_management")
    object TimesheetEntry : Routes("timesheet_entry")
    object WhatIfScenarioPlanner : Routes("whatif_scenario_planner")
    object BudgetOverview : Routes("budget_overview")
    object Reports : Routes("reports")
    object NotificationsCenter : Routes("notifications_center")
    object Settings : Routes("settings")
    object Search : Routes("search")
    object Profile : Routes("profile")
    object Help : Routes("help")

    // Pre-existing screen, not one of the 14 target screens but kept reachable via the
    // top-bar overflow menu (see AppRoot.kt) rather than dropped.
    object Requests : Routes("requests")
}

data class BottomNavDestination(val routes: Routes, val label: String)

val bottomNavDestinations = listOf(
    BottomNavDestination(Routes.Dashboard, "Dashboard"),
    BottomNavDestination(Routes.ResourceList, "Resources"),
    BottomNavDestination(Routes.ProjectList, "Projects"),
    BottomNavDestination(Routes.ResourceCalendar, "Calendar"),
    BottomNavDestination(Routes.Reports, "Reports")
)
