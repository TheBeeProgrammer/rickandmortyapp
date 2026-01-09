package com.renato.data.di

import com.renato.data.api.entities.CharacterListResponse
import com.renato.data.mapper.CharacterListResponseMapper
import com.renato.data.mapper.Mapper
import com.renato.domain.model.character.PaginatedCharacter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {
    /**
     * Provides a Mapper that converts a CharacterListResponse into a PaginatedCharacter for injection.
     *
     * @return A Mapper that transforms `CharacterListResponse` instances into `PaginatedCharacter` instances.
     */
    @Provides
    fun provideApiCharacterResponseMapper(): Mapper<CharacterListResponse, PaginatedCharacter> {
        return CharacterListResponseMapper()
    }
}