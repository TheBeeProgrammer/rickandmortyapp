package com.renato.rickandmorty

import android.content.Context
import com.renato.data.api.interceptor.ConnectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    /**
     * Provides a singleton ConnectionManager initialized with the application context.
     *
     * @param context The application Context used to create the ConnectionManager.
     * @return A ConnectionManager instance configured with the provided context.
     */
    @Provides
    @Singleton
    fun provideConnectionManager(@ApplicationContext context: Context): ConnectionManager {
        return ConnectionManager(context)
    }
}