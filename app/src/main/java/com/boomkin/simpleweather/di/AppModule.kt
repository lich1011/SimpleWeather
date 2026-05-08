package com.boomkin.simpleweather.di

import android.app.Application
import androidx.room.Room
import com.boomkin.simpleweather.data.local.WeatherDatabase
import com.boomkin.simpleweather.data.local.dao.CityDao
import com.boomkin.simpleweather.data.local.dao.WeatherRecordDao
import com.boomkin.simpleweather.data.remote.WeatherApi
import com.boomkin.simpleweather.data.repository.WeatherRepositoryImpl
import com.boomkin.simpleweather.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi {
        return Retrofit.Builder()
            .baseUrl(WeatherApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherDatabase(app: Application): WeatherDatabase {
        return Room.databaseBuilder(
            app,
            WeatherDatabase::class.java,
            "weather_db"
        ).fallbackToDestructiveMigration()
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
    fun provideWeatherRepository(
        api: WeatherApi,
        cityDao: CityDao,
        weatherRecordDao: WeatherRecordDao
    ): WeatherRepository {
        // Use Mock data for manual UI testing in VM
        return com.boomkin.simpleweather.data.repository.FakeWeatherRepositoryImpl()
        // Uncomment to use real data:
        // return WeatherRepositoryImpl(api, cityDao, weatherRecordDao)
    }
}
