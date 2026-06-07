package com.boomkin.simpleweather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geocoding_cache")
data class GeocodingEntity(
    @PrimaryKey val query: String, // Lowercase query string (e.g. "beijing")
    val cityName: String,        // Canonical city name (e.g. "Beijing")
    val country: String,         // Country name
    val latitude: Double,
    val longitude: Double,
    val cachedAt: Long = System.currentTimeMillis()
)
