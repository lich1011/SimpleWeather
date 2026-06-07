package com.boomkin.simpleweather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boomkin.simpleweather.data.local.entity.GeocodingEntity

@Dao
interface GeocodingDao {
    @Query("SELECT * FROM geocoding_cache WHERE `query` = :query LIMIT 1")
    suspend fun getByQuery(query: String): GeocodingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(geocoding: GeocodingEntity)
}
