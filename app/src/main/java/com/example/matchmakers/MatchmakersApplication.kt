package com.example.matchmakers

import android.app.Application
import androidx.work.*
import com.example.matchmakers.di.appModule
import com.example.matchmakers.di.databaseModule
import com.example.matchmakers.worker.UserSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class MatchmakersApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin Dependency Injection
        startKoin {
            androidContext(this@MatchmakersApplication) // Set application context
            modules(listOf(appModule, databaseModule)) // Load Koin modules
        }

        // Schedule periodic updates using WorkManager
        setupPeriodicUserSync()
    }

    private fun setupPeriodicUserSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Network must be connected
            .setRequiresBatteryNotLow(true) // Avoid running on low battery
            .build()

        val workRequest = PeriodicWorkRequestBuilder<UserSyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "UserSyncWorker", // Unique name
            ExistingPeriodicWorkPolicy.KEEP, // Don't duplicate if already running
            workRequest
        )
    }
}
