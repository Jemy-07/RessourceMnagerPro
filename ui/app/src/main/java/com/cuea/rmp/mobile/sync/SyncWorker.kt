package com.cuea.rmp.mobile.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cuea.rmp.mobile.request.RequestRepository
import com.cuea.rmp.mobile.timesheet.TimesheetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val timesheetRepository: TimesheetRepository,
    private val requestRepository: RequestRepository,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            // REST-replay queue (creates only, nothing to conflict against).
            timesheetRepository.syncPendingTimesheets()
            requestRepository.syncPendingRequests()
            // Generic /sync/push engine (edits to existing rows — version-aware,
            // surfaces real conflicts; see SyncRepository's doc comment for why these
            // two domains can't use the REST-replay pattern above).
            syncRepository.pushPendingMutations()
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

