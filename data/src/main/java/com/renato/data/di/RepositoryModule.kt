package com.renato.data.di

import com.renato.data.api.RickAndMortyApiService
import com.renato.data.api.entities.CharacterListResponse
import com.renato.data.mapper.Mapper
import com.renato.data.repository.CharacterRepositoryImpl
import com.renato.domain.IoDispatcher
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.repositories.CharacterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCharacterRepository(
        apiService: RickAndMortyApiService,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        mapper: Mapper<CharacterListResponse, PaginatedCharacter>
    ): CharacterRepository {
        return CharacterRepositoryImpl(
            apiService = apiService,
            ioDispatcher = ioDispatcher,
            mapper = mapper
        )
    }
}