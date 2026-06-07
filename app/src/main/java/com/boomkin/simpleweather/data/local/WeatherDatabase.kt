package com.boomkin.simpleweather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boomkin.simpleweather.data.local.dao.CachedWeatherDao
import com.boomkin.simpleweather.data.local.dao.CityDao
import com.boomkin.simpleweather.data.local.dao.WeatherRecordDao
import com.boomkin.simpleweather.data.local.entity.CachedWeatherEntity
import com.boomkin.simpleweather.data.local.entity.CityEntity
import com.boomkin.simpleweather.data.local.entity.WeatherRecordEntity

@Database(
    entities = [WeatherRecordEntity::class, CityEntity::class, CachedWeatherEntity::class],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherRecordDao(): WeatherRecordDao
    abstract fun cityDao(): CityDao
    abstract fun cachedWeatherDao(): CachedWeatherDao

}