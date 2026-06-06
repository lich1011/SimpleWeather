package com.boomkin.simpleweather.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boomkin.simpleweather.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.io.IOException

@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.i("WeatherSyncWorker: Starting periodic background sync...")
        return try {
            weatherRepository.refreshAllCities()
            Timber.i("WeatherSyncWorker: Background sync completed successfully.")
            Result.success()
        } catch (e: IOException) {
            // 网络错误属于可恢复错误，允许重试
            Timber.w(e, "WeatherSyncWorker: Network error, will retry.")
            Result.retry()
        } catch (e: Exception) {
            // 数据解析、城市不存在等不可恢复错误，不再重试
            Timber.e(e, "WeatherSyncWorker: Non-recoverable error, giving up.")
            Result.failure()
        }
    }
}
