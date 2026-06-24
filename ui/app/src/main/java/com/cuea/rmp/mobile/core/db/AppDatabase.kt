package com.cuea.rmp.mobile.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cuea.rmp.mobile.notification.NotificationDao
import com.cuea.rmp.mobile.notification.NotificationLocalEntity
import com.cuea.rmp.mobile.resource.ResourceDao
import com.cuea.rmp.mobile.resource.ResourceLocalEntity
import com.cuea.rmp.mobile.timesheet.TimesheetDao
import com.cuea.rmp.mobile.timesheet.TimesheetLocalEntity

@Database(
    entities = [
        PendingMutationEntity::class,
        TimesheetLocalEntity::class,
        NotificationLocalEntity::class,
        ResourceLocalEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pendingMutationDao(): PendingMutationDao

    abstract fun timesheetDao(): TimesheetDao

    abstract fun notificationDao(): NotificationDao

    abstract fun resourceDao(): ResourceDao
}


