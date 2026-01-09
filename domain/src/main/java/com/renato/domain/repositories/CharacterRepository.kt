package com.renato.domain.repositories

import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.usecases.UseCaseResult
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    suspend fun requestCharacters(page: Int): UseCaseResult<Flow<PaginatedCharacter>>
}