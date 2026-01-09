package com.renato.data.di

import com.renato.data.api.entities.CharacterListResponse
import com.renato.data.mapper.CharacterListResponseMapper
import com.renato.data.mapper.Mapper
import com.renato.domain.model.character.PaginatedCharacter
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Provides
fun provideApiCharacterResponseMapper(): Mapper<CharacterListResponse, PaginatedCharacter> {
    return CharacterListResponseMapper()
}
