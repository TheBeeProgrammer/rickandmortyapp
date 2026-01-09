package com.renato.data.repository

import com.renato.data.api.RickAndMortyApiService
import com.renato.data.api.entities.CharacterListResponse
import com.renato.data.mapper.Mapper
import com.renato.data.repository.base.BaseRepository
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.repositories.CharacterRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [CharacterRepository] that fetches character data from the Rick and Morty API.
 *
 * @property apiService The retrofit service used to make network requests.
 * @property ioDispatcher The coroutine dispatcher used to perform the network operations on a background thread.
 */
class CharacterRepositoryImpl
@Inject
constructor(
    private val apiService: RickAndMortyApiService,
    private val ioDispatcher: CoroutineDispatcher,
    private val mapper: Mapper<CharacterListResponse, PaginatedCharacter>,
) : CharacterRepository, BaseRepository() {

    /**
     * Fetches a page of characters from the remote API and emits the mapped paginated result.
     *
     * @param page The page number to request from the API.
     * @return A Flow that emits the PaginatedCharacter for the requested page.
     */
    override suspend fun requestCharacters(page: Int) = safeCall {
        val response =
            withContext(ioDispatcher) {
                apiService.getCharacters(page)
            }
        val characters = mapper.map(from = response, currentPage = page)
        flowOf(characters)
    }
}