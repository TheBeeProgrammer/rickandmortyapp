package com.renato.domain.usecases.characters

import com.renato.domain.model.NetworkUnavailableException
import com.renato.domain.model.NoMoreCharactersException
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.repositories.CharacterRepository
import com.renato.domain.usecases.base.ExecutableUseCase
import com.renato.domain.usecases.base.UseCaseResult
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

class RequestNextPageOfCharacters @Inject constructor(private val repository: CharacterRepository) :
    ExecutableUseCase<Int, PaginatedCharacter> {
    /**
     * Requests the next page of characters from the repository and returns the retrieved paginated result.
     *
     * @param params The page index to request.
     * @return The PaginatedCharacter for the requested page.
     * @throws NoMoreCharactersException if the retrieved page contains no characters.
     * @throws NetworkUnavailableException if the request failed due to lack of internet connectivity.
     * @throws Exception with the repository-provided message for unknown failure reasons.
     */
    override suspend fun invoke(params: Int): PaginatedCharacter {
        return when (val result = repository.requestCharacters(params)) {
            is UseCaseResult.Success -> {
                val paginatedCharacter = result.data.first()
                if (paginatedCharacter.characters.isEmpty()) {
                    throw NoMoreCharactersException()
                }
                paginatedCharacter
            }

            is UseCaseResult.Failure -> {
                when (result.reason) {
                    UseCaseResult.Reason.NoInternet -> throw NetworkUnavailableException()
                    is UseCaseResult.Reason.Unknown -> throw Exception(result.reason.message)
                }
            }
        }

    }
}