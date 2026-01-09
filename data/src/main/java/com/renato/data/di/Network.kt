package com.renato.data.di

import com.renato.data.BuildConfig
import com.renato.data.api.ApiConstants.BASE_URL
import com.renato.data.api.RickAndMortyApiService
import com.renato.data.api.interceptor.ConnectionManager
import com.renato.data.api.interceptor.LoggingInterceptor
import com.renato.data.api.interceptor.NetworkStatusInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Network {
    @Provides
    @Singleton
    fun provideNetworkStatusInterceptor(
        connectionManager: ConnectionManager
    ): NetworkStatusInterceptor = NetworkStatusInterceptor(connectionManager)

    @Provides
    @Singleton
    fun provideLoggingInterceptor(logger: LoggingInterceptor): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(logger).apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }

    @Provides
    @Singleton
    fun provideCustomLogger(): LoggingInterceptor = LoggingInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        networkStatusInterceptor: NetworkStatusInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(networkStatusInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRickAndMortyApiService(retrofit: Retrofit): RickAndMortyApiService {
        return retrofit.create(RickAndMortyApiService::class.java)
    }
}

