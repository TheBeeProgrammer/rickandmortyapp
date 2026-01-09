package com.renato.rickandmorty.di

import android.content.Context
import com.renato.data.api.interceptor.ConnectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    @Provides
    @Singleton
    fun provideConnectionManager(@ApplicationContext context: Context): ConnectionManager {
        return ConnectionManager(context)
    }
}