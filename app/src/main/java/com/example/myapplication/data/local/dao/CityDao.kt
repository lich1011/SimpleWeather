package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM cities ORDER BY addedAt DESC")
    fun getAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities ORDER BY isDefault Desc, addedAt DESC")
    suspend fun getDefaultCity(): CityEntity?

    @Query("SELECT * FROM cities WHERE name = :cityName")
    suspend fun getCityByName(cityName: String): CityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity): Long

    @Delete
    suspend fun deleteCity(city: CityEntity)

    @Query("UPDATE cities SET isDefault = CASE WHEN id = :cityId THEN 1 ELSE 0 END")
    suspend fun setDefaultCity(cityId: Int)
}
