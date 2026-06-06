package com.boomkin.simpleweather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boomkin.simpleweather.data.local.entity.CachedWeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedWeatherDao {
    @Query("SELECT * FROM cached_weather WHERE cityName = :cityName LIMIT 1")
    suspend fun getCachedWeather(cityName: String): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather WHERE cityName = :cityName LIMIT 1")
    fun getCachedWeatherFlow(cityName: String): Flow<CachedWeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedWeather(entity: CachedWeatherEntity)

    @Query("DELETE FROM cached_weather WHERE cityName = :cityName")
    suspend fun deleteCachedWeather(cityName: String)
}
