package com.renato.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    /**
 * Provides the dispatcher optimized for IO-bound coroutine work.
 *
 * @return The IO `CoroutineDispatcher` (Dispatchers.IO).
 */
@Provides fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}