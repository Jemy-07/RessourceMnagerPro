package com.cuea.rmp.mobile.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.cuea.rmp.mobile.core.db.AppDatabase
import com.cuea.rmp.mobile.core.db.PendingMutationDao
import com.cuea.rmp.mobile.notification.NotificationDao
import com.cuea.rmp.mobile.project.ProjectDao
import com.cuea.rmp.mobile.request.RequestDao
import com.cuea.rmp.mobile.resource.ResourceDao
import com.cuea.rmp.mobile.timesheet.TimesheetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rmp_mobile.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePendingMutationDao(appDatabase: AppDatabase): PendingMutationDao {
        return appDatabase.pendingMutationDao()
    }

    @Provides
    fun provideTimesheetDao(appDatabase: AppDatabase): TimesheetDao {
        return appDatabase.timesheetDao()
    }

    @Provides
    fun provideNotificationDao(appDatabase: AppDatabase): NotificationDao {
        return appDatabase.notificationDao()
    }

    @Provides
    fun provideResourceDao(appDatabase: AppDatabase): ResourceDao {
        return appDatabase.resourceDao()
    }

    @Provides
    fun provideProjectDao(appDatabase: AppDatabase): ProjectDao {
        return appDatabase.projectDao()
    }

    @Provides
    fun provideRequestDao(appDatabase: AppDatabase): RequestDao {
        return appDatabase.requestDao()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}

