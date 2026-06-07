package com.boomkin.simpleweather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.boomkin.simpleweather.data.local.dao.CachedWeatherDao
import com.boomkin.simpleweather.data.local.dao.CityDao
import com.boomkin.simpleweather.data.local.dao.GeocodingDao
import com.boomkin.simpleweather.data.local.dao.WeatherRecordDao
import com.boomkin.simpleweather.data.local.entity.CachedWeatherEntity
import com.boomkin.simpleweather.data.local.entity.CityEntity
import com.boomkin.simpleweather.data.local.entity.GeocodingEntity
import com.boomkin.simpleweather.data.local.entity.WeatherRecordEntity

@Database(
    entities = [WeatherRecordEntity::class, CityEntity::class, CachedWeatherEntity::class, GeocodingEntity::class],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherRecordDao(): WeatherRecordDao
    abstract fun cityDao(): CityDao
    abstract fun cachedWeatherDao(): CachedWeatherDao
    abstract fun geocodingDao(): GeocodingDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No changes between v5 and v6
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `geocoding_cache` (`query` TEXT NOT NULL, `cityName` TEXT NOT NULL, `country` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`query`))"
                )
                // 2. Backfill from existing cities table
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `geocoding_cache` (`query`, `cityName`, `country`, `latitude`, `longitude`, `cachedAt`)
                    SELECT LOWER(name), name, country, latitude, longitude, strftime('%s', 'now') * 1000
                    FROM cities
                    """.trimIndent()
                )
            }
        }
    }
}