package com.cuea.rmp.mobile.core.db

import androidx.room.TypeConverter
import com.cuea.rmp.mobile.timesheet.LocalSyncState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class Converters {

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun instantToString(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun stringToInstant(value: String?): Instant? = value?.let { Instant.parse(it) }

    @TypeConverter
    fun pendingMutationStatusToString(value: PendingMutationStatus?): String? = value?.name

    @TypeConverter
    fun stringToPendingMutationStatus(value: String?): PendingMutationStatus? {
        return value?.let { PendingMutationStatus.valueOf(it) }
    }

    @TypeConverter
    fun localSyncStateToString(value: LocalSyncState?): String? = value?.name

    @TypeConverter
    fun stringToLocalSyncState(value: String?): LocalSyncState? {
        return value?.let { LocalSyncState.valueOf(it) }
    }
}

