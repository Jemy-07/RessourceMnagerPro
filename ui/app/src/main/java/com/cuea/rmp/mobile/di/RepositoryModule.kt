package com.cuea.rmp.mobile.di

import com.cuea.rmp.mobile.auth.AuthApi
import com.cuea.rmp.mobile.auth.AuthRepository
import com.cuea.rmp.mobile.auth.TokenManager
import com.cuea.rmp.mobile.budget.BudgetApi
import com.cuea.rmp.mobile.budget.BudgetDao
import com.cuea.rmp.mobile.budget.BudgetRepository
import com.cuea.rmp.mobile.notification.NotificationApi
import com.cuea.rmp.mobile.notification.NotificationDao
import com.cuea.rmp.mobile.notification.NotificationRepository
import com.cuea.rmp.mobile.project.AssignmentApi
import com.cuea.rmp.mobile.project.AssignmentDao
import com.cuea.rmp.mobile.project.AssignmentRepository
import com.cuea.rmp.mobile.project.ProjectApi
import com.cuea.rmp.mobile.project.ProjectDao
import com.cuea.rmp.mobile.project.ProjectRepository
import com.cuea.rmp.mobile.request.RequestApi
import com.cuea.rmp.mobile.request.RequestDao
import com.cuea.rmp.mobile.request.RequestRepository
import com.cuea.rmp.mobile.resource.ResourceApi
import com.cuea.rmp.mobile.resource.ResourceDao
import com.cuea.rmp.mobile.resource.ResourceRepository
import com.cuea.rmp.mobile.timesheet.TimesheetApi
import com.cuea.rmp.mobile.timesheet.TimesheetDao
import com.cuea.rmp.mobile.timesheet.TimesheetRepository
import com.cuea.rmp.mobile.user.UserApi
import com.cuea.rmp.mobile.user.UserDao
import com.cuea.rmp.mobile.user.UserRepository
import com.cuea.rmp.mobile.core.db.PendingMutationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenManager: TokenManager,
        json: Json
    ): AuthRepository {
        return AuthRepository(authApi, tokenManager, json)
    }

    @Provides
    @Singleton
    fun provideTimesheetRepository(
        timesheetApi: TimesheetApi,
        timesheetDao: TimesheetDao,
        pendingMutationDao: PendingMutationDao,
        json: Json
    ): TimesheetRepository {
        return TimesheetRepository(timesheetApi, timesheetDao, pendingMutationDao, json)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationApi: NotificationApi,
        notificationDao: NotificationDao,
        json: Json
    ): NotificationRepository {
        return NotificationRepository(notificationApi, notificationDao, json)
    }

    @Provides
    @Singleton
    fun provideResourceRepository(
        resourceApi: ResourceApi,
        resourceDao: ResourceDao,
        json: Json
    ): ResourceRepository {
        return ResourceRepository(resourceApi, resourceDao, json)
    }

    @Provides
    @Singleton
    fun provideProjectRepository(
        projectApi: ProjectApi,
        projectDao: ProjectDao,
        json: Json
    ): ProjectRepository {
        return ProjectRepository(projectApi, projectDao, json)
    }

    @Provides
    @Singleton
    fun provideRequestRepository(
        requestApi: RequestApi,
        requestDao: RequestDao,
        json: Json
    ): RequestRepository {
        return RequestRepository(requestApi, requestDao, json)
    }

    @Provides
    @Singleton
    fun provideAssignmentRepository(
        assignmentApi: AssignmentApi,
        projectApi: ProjectApi,
        assignmentDao: AssignmentDao,
        json: Json
    ): AssignmentRepository {
        return AssignmentRepository(assignmentApi, projectApi, assignmentDao, json)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetApi: BudgetApi,
        budgetDao: BudgetDao,
        json: Json
    ): BudgetRepository {
        return BudgetRepository(budgetApi, budgetDao, json)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userApi: UserApi,
        userDao: UserDao,
        json: Json
    ): UserRepository {
        return UserRepository(userApi, userDao, json)
    }
}

