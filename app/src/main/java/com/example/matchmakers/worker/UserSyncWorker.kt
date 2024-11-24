package com.example.matchmakers.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.matchmakers.domain.UserService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.util.Log

class UserSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val userService: UserService by inject() // Dependency injected UserService
    private val TAG = "UserSyncWorker"

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting user cache sync...")
            userService.getRecommendedUsers() // Updates the cache
            Log.d(TAG, "User cache sync completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing users: ${e.message}")
            Result.retry() // Retry the work in case of failure
        }
    }
}
