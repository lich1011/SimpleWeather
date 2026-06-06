package com.boomkin.simpleweather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.boomkin.simpleweather.data.local.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CityDao {
    @Query("SELECT * FROM cities WHERE isActive = 1 ORDER BY isDefault DESC, addedAt ASC")
    abstract fun getAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE isActive = 1 ORDER BY isDefault DESC, addedAt ASC")
    abstract suspend fun getAllCitiesSync(): List<CityEntity>

    @Query("SELECT * FROM cities WHERE isActive = 0 ORDER BY addedAt DESC")
    abstract fun getArchivedCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE isDefault = 1 LIMIT 1")
    abstract suspend fun getDefaultCity(): CityEntity?

    @Query("SELECT * FROM cities WHERE name = :cityName")
    abstract suspend fun getCityByName(cityName: String): CityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCity(city: CityEntity): Long

    @Query("UPDATE cities SET isActive = 0, isDefault = 0 WHERE id = :cityId")
    abstract suspend fun softDeleteCity(cityId: Int)

    @Query("UPDATE cities SET isActive = 1, addedAt = :time WHERE id = :cityId")
    abstract suspend fun reactivateCity(cityId: Int, time: Long)

    @Query("UPDATE cities SET isDefault = 0 WHERE isDefault = 1")
    protected abstract suspend fun clearAllDefaults()

    @Query("UPDATE cities SET isDefault = 1 WHERE id = :cityId")
    protected abstract suspend fun markCityAsDefault(cityId: Int)

    /**
     * Set a city as the default using two targeted UPDATEs within a transaction,
     * instead of a single full-table UPDATE.
     */
    @Transaction
    open suspend fun setDefaultCity(cityId: Int) {
        clearAllDefaults()
        markCityAsDefault(cityId)
    }
}
