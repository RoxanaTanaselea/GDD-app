package net.aiscope.gdd_app.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import net.aiscope.gdd_app.repository.SampleRepository

class UploadWorker constructor(
        context: Context,
        params: WorkerParameters,
        val repo: SampleRepository,
        val storage: RemoteStorage) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result {
        val id = inputData.getString("sample_id")

        return when(id) {
            null -> Result.failure()
            else -> {
                try {
                    val sample = repo.load(id)

                    storage.upload(sample)
                    Result.success()
                } catch (error: Throwable) {
                    Result.retry()
                }
            }
        }
    }
}
