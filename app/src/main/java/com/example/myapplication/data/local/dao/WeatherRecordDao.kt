package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.WeatherRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherRecordDao {
//    @Query("SELECT * FROM weather_records ORDER BY timestamp DESC LIMIT 1")
//    fun getRecordForCity(city: String): Flow<List<WeatherRecordEntity>>

    @Insert
    suspend fun insert(record: WeatherRecordEntity)
}