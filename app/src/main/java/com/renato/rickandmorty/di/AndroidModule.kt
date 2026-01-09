package com.renato.rickandmorty.di

import android.content.Context
import com.renato.data.api.interceptor.ConnectionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AndroidModule {
    @Binds
    @Singleton
    abstract fun bindResourceProvider(impl: ResourceProviderImpl): ResourceProvider

    companion object {
        @Provides
        @Singleton
        fun provideConnectionManager(@ApplicationContext context: Context): ConnectionManager {
            return ConnectionManager(context)
        }
    }
}