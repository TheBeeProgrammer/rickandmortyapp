package com.renato.data.di

import com.renato.data.api.RickAndMortyApiService
import com.renato.data.api.entities.CharacterListResponse
import com.renato.data.mapper.Mapper
import com.renato.data.repository.CharacterRepositoryImpl
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.repositories.CharacterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides a CharacterRepository implementation configured with the given dependencies.
     *
     * @param apiService Client used to fetch character data from the Rick and Morty API.
     * @param ioDispatcher Coroutine dispatcher used for IO-bound work within the repository.
     * @param mapper Maps API character list responses to the domain PaginatedCharacter model.
     * @return A CharacterRepository backed by CharacterRepositoryImpl configured with the provided dependencies.
     */
    @Provides
    fun provideCharacterRepository(
        apiService: RickAndMortyApiService,
        ioDispatcher: CoroutineDispatcher,
        mapper: Mapper<CharacterListResponse, PaginatedCharacter>
    ): CharacterRepository {
        return CharacterRepositoryImpl(
            apiService = apiService,
            ioDispatcher = ioDispatcher,
            mapper = mapper
        )
    }
}