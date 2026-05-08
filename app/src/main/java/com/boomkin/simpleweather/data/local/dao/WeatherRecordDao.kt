package com.boomkin.simpleweather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boomkin.simpleweather.data.local.entity.WeatherRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherRecordDao {
    @Query("SELECT * FROM weather_records WHERE cityName = :cityName ORDER BY timestamp DESC")
    fun getRecordForCity(cityName: String): Flow<List<WeatherRecordEntity>>

    @Insert
    suspend fun insert(record: WeatherRecordEntity)
}