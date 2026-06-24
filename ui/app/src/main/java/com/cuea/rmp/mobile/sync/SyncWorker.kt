package com.cuea.rmp.mobile.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cuea.rmp.mobile.timesheet.TimesheetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val timesheetRepository: TimesheetRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            timesheetRepository.syncPendingTimesheets()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_PERIODIC_WORK_NAME: String = "rmp_periodic_sync"
        const val IMMEDIATE_WORK_NAME: String = "rmp_one_shot_sync"
    }
}

