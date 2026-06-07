package com.boomkin.simpleweather.di

import android.app.Application
import androidx.room.Room
import com.boomkin.simpleweather.data.local.WeatherDatabase
import com.boomkin.simpleweather.data.local.dao.CachedWeatherDao
import com.boomkin.simpleweather.data.local.dao.CityDao
import com.boomkin.simpleweather.data.local.dao.GeocodingDao
import com.boomkin.simpleweather.data.local.dao.WeatherRecordDao
import com.boomkin.simpleweather.data.remote.WeatherApi
import com.boomkin.simpleweather.data.repository.WeatherRepositoryImpl
import com.boomkin.simpleweather.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApi(client: OkHttpClient): WeatherApi {
        return Retrofit.Builder()
            .baseUrl(WeatherApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideWeatherDatabase(app: Application): WeatherDatabase {
        return Room.databaseBuilder(
            app,
            WeatherDatabase::class.java,
            "weather_db"
        )
            .addMigrations(WeatherDatabase.MIGRATION_5_6, WeatherDatabase.MIGRATION_6_7)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideCityDao(db: WeatherDatabase): CityDao {
        return db.cityDao()
    }

    @Provides
    @Singleton
    fun provideWeatherRecordDao(db: WeatherDatabase): WeatherRecordDao {
        return db.weatherRecordDao()
    }

    @Provides
    @Singleton
    fun provideCachedWeatherDao(db: WeatherDatabase): CachedWeatherDao {
        return db.cachedWeatherDao()
    }

    @Provides
    @Singleton
    fun provideGeocodingDao(db: WeatherDatabase): GeocodingDao {
        return db.geocodingDao()
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        api: WeatherApi,
        cityDao: CityDao,
        weatherRecordDao: WeatherRecordDao,
        cachedWeatherDao: CachedWeatherDao,
        geocodingDao: GeocodingDao
    ): WeatherRepository {
        return WeatherRepositoryImpl(api, cityDao, weatherRecordDao, cachedWeatherDao, geocodingDao)
    }
}
