package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.CityDao
import com.example.myapplication.data.local.dao.WeatherRecordDao
import com.example.myapplication.data.local.entity.CityEntity
import com.example.myapplication.data.local.entity.WeatherRecordEntity

@Database(
    entities = [WeatherRecordEntity::class, CityEntity::class],
    version = 2,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherRecordDao(): WeatherRecordDao
    abstract fun cityDao(): CityDao

}