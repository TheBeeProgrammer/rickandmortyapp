package com.renato.data.repository

import com.renato.data.api.RickAndMortyApiService
import com.renato.data.api.entities.CharacterListResponse
import com.renato.data.mapper.Mapper
import com.renato.data.repository.base.BaseRepository
import com.renato.domain.IoDispatcher
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.repositories.CharacterRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [CharacterRepository] responsible for fetching and mapping character data
 * from the Rick and Morty API.
 *
 * This class leverages [BaseRepository] for safe network execution and handles the transformation
 * from data layer entities to domain models.
 *
 * @property apiService The [RickAndMortyApiService] used to perform network requests.
 * @property ioDispatcher The [CoroutineDispatcher] used to execute network operations on a background thread.
 * @property mapper The [Mapper] used to convert [CharacterListResponse] into [PaginatedCharacter] domain objects.
 */
class CharacterRepositoryImpl
@Inject
constructor(
    private val apiService: RickAndMortyApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val mapper: Mapper<CharacterListResponse, PaginatedCharacter>,
) : CharacterRepository, BaseRepository() {

    override suspend fun requestCharacters(page: Int) = safeCall {
        val response =
            withContext(ioDispatcher) {
                apiService.getCharacters(page)
            }
        val characters = mapper.map(from = response, currentPage = page)
        flowOf(characters)
    }
}
