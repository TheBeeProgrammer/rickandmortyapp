package com.renato.data.di

import com.renato.data.api.RickAndMortyApiService
import com.renato.data.repository.CharacterRepositoryImpl
import com.renato.domain.repositories.CharacterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideCharacterRepository(
        apiService: RickAndMortyApiService,
        ioDispatcher: CoroutineDispatcher
    ): CharacterRepository {
        return CharacterRepositoryImpl(apiService, ioDispatcher)
    }
}