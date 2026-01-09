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
    /**
     * Creates a NetworkStatusInterceptor that uses the provided ConnectionManager to determine network availability.
     *
     * @return A `NetworkStatusInterceptor` that checks network connectivity via the supplied `ConnectionManager`.
     */
    @Provides
    @Singleton
    fun provideNetworkStatusInterceptor(
        connectionManager: ConnectionManager
    ): NetworkStatusInterceptor = NetworkStatusInterceptor(connectionManager)

    /**
     * Creates an HttpLoggingInterceptor that forwards log events to the provided LoggingInterceptor.
     *
     * The interceptor's level is set to BODY when BuildConfig.DEBUG is true, and to BASIC otherwise.
     *
     * @param logger The LoggingInterceptor that will receive logging callbacks from the HttpLoggingInterceptor.
     * @return An HttpLoggingInterceptor configured with the provided logger and an appropriate logging level.
     */
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

    /**
     * Provides a LoggingInterceptor instance used for HTTP request/response logging.
     *
     * @return A new `LoggingInterceptor` instance.
     */
    @Provides
    @Singleton
    fun provideCustomLogger(): LoggingInterceptor = LoggingInterceptor()

    /**
     * Provides a singleton OkHttpClient configured with the supplied interceptors.
     *
     * @param networkStatusInterceptor Interceptor that enforces network availability for requests.
     * @param loggingInterceptor Interceptor that logs HTTP requests and responses.
     * @return An OkHttpClient instance configured with the provided interceptors.
     */
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

    /**
     * Provides a Retrofit instance configured for the API.
     *
     * The instance uses the module's BASE_URL and GsonConverterFactory for JSON (de)serialization.
     *
     * @return A Retrofit instance configured with BASE_URL and GsonConverterFactory.
     */
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Creates an implementation of RickAndMortyApiService using the provided Retrofit instance.
     *
     * @return A `RickAndMortyApiService` implementation backed by the given `Retrofit`.
     */
    @Provides
    @Singleton
    fun provideRickAndMortyApiService(retrofit: Retrofit): RickAndMortyApiService {
        return retrofit.create(RickAndMortyApiService::class.java)
    }
}
