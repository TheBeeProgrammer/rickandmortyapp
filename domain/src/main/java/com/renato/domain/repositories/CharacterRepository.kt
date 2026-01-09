package com.renato.domain.repositories

import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.usecases.base.UseCaseResult
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    /**
 * Requests a specific page of characters as a stream of paginated results.
 *
 * @param page Index of the page to request.
 * @return A `UseCaseResult` wrapping a `Flow` that emits `PaginatedCharacter` pages for the requested page. 
 */
suspend fun requestCharacters(page: Int): UseCaseResult<Flow<PaginatedCharacter>>
}