package com.cuea.rmp.mobile.sync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.cuea.rmp.mobile.RmpApplication
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression test for the Urgent Cleanup Half-Sprint WorkManager bug: the default
 * androidx.startup WorkManagerInitializer ran before Hilt injected
 * RmpApplication.workerFactory (ContentProviders always initialize before
 * Application.onCreate()), so WorkManager silently fell back to its default
 * WorkerFactory and every real SyncWorker instantiation failed with
 * NoSuchMethodException trying to use a plain (Context, WorkerParameters)
 * constructor it doesn't have.
 *
 * This deliberately reuses the REAL app's Configuration.Provider (the actual
 * Hilt-wired HiltWorkerFactory and real repositories pointed at the dev backend)
 * rather than a synthetic test factory, because the bug was specifically in that
 * wiring -- a fake factory would pass even with the bug present.
 */
@RunWith(AndroidJUnit4::class)
class SyncWorkerInstrumentedTest {

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<RmpApplication>()
        val config = Configuration.Builder()
            .setWorkerFactory(app.workManagerConfiguration.workerFactory)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(app, config)
    }

    @Test
    fun syncWorker_isInstantiatedByTheRealHiltWorkerFactory_andRuns() {
        val context = ApplicationProvider.getApplicationContext<RmpApplication>()
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()

        workManager.enqueue(request).result.get()

        val driver = WorkManagerTestInitHelper.getTestDriver(context)
        driver?.setAllConstraintsMet(request.id)

        val info = workManager.getWorkInfoById(request.id).get()

        // The original bug surfaced as WorkManager being unable to construct SyncWorker
        // at all (NoSuchMethodException from the default factory), which WorkManager
        // reports as the work going to FAILED. Reaching any other state (SUCCEEDED, or
        // ENQUEUED/RUNNING again after a network-related Result.retry()) proves the real
        // Hilt factory actually built a real SyncWorker instance and ran doWork().
        assertNotEquals(WorkInfo.State.FAILED, info.state)
    }
}
