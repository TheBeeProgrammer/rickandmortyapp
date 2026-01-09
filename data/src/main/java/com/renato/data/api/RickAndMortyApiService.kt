package com.renato.data.api

import com.renato.data.api.ApiConstants.DEFAULT_PAGE
import com.renato.data.api.entities.CharacterListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface for interacting with the Rick and Morty API
 *
 * @see <a href="https://rickandmortyapi.com/documentations">Rick and Morty API Documentation</a>
 */
interface RickAndMortyApiService {

    /**
     * Get a paginated list of characters
     *
     * @param page Page number (starts at 1)
     */
    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int = DEFAULT_PAGE
    ): CharacterListResponse
}
