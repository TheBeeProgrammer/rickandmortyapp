package com.renato.domain.usecases.characters

import com.renato.domain.model.character.Character
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.repositories.CharacterRepository
import com.renato.domain.usecases.base.UseCaseResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RequestNextPageOfCharacters @Inject constructor(
    private val repository: CharacterRepository
) : PaginatedCharactersUseCase {

    private var currentPage = 1
    private var hasMorePages = true
    private val accumulatedCharacters = mutableListOf<Character>()

    override suspend fun invoke(): UseCaseResult<PaginatedCharacter> {
        if (!hasMorePages) {
            return UseCaseResult.Failure(UseCaseResult.Reason.NoMoreCharacters)
        }

        return when (val result = repository.requestCharacters(currentPage)) {
            is UseCaseResult.Success -> {
                val newPaginatedResult = result.data.first()
                hasMorePages = newPaginatedResult.pagination.hasNextPage
                accumulatedCharacters.addAll(newPaginatedResult.characters)

                if (accumulatedCharacters.isEmpty()) {
                    UseCaseResult.Failure(UseCaseResult.Reason.NoMoreCharacters)
                } else {
                    val fullPaginatedResult = newPaginatedResult.copy(characters = accumulatedCharacters)
                    currentPage++
                    UseCaseResult.Success(fullPaginatedResult)
                }
            }
            is UseCaseResult.Failure -> result
        }
    }

    override fun reset() {
        currentPage = 1
        hasMorePages = true
        accumulatedCharacters.clear()
    }
}