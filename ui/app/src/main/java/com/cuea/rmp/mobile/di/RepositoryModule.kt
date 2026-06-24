package com.cuea.rmp.mobile.di

import com.cuea.rmp.mobile.auth.AuthApi
import com.cuea.rmp.mobile.auth.AuthRepository
import com.cuea.rmp.mobile.auth.TokenManager
import com.cuea.rmp.mobile.notification.NotificationApi
import com.cuea.rmp.mobile.notification.NotificationDao
import com.cuea.rmp.mobile.notification.NotificationRepository
import com.cuea.rmp.mobile.timesheet.TimesheetApi
import com.cuea.rmp.mobile.timesheet.TimesheetDao
import com.cuea.rmp.mobile.timesheet.TimesheetRepository
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
}

