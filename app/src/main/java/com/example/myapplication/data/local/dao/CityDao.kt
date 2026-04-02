package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.myapplication.data.local.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
//    @Query("SELECT * FROM cities")
//    fun getAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities ORDER BY isDefault Desc, addedAt DESC")
    suspend fun getDefaultCity(): CityEntity?

    @Query("SELECT * FROM cities WHERE name = :cityName")
    suspend fun getCityByName(cityName: String): CityEntity?

}


