package com.boomkin.simpleweather.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cities", indices = [Index(value = ["name"], unique = true)])
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val country: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

