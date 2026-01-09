package com.renato.domain.usecases.characters

import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.repositories.CharacterRepository
import com.renato.domain.usecases.base.ExecutableUseCase
import com.renato.domain.usecases.base.UseCaseResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RequestNextPageOfCharacters @Inject constructor(private val repository: CharacterRepository) :
    ExecutableUseCase<Int, UseCaseResult<PaginatedCharacter>> {
    override suspend fun invoke(params: Int): UseCaseResult<PaginatedCharacter> {
        return when (val result = repository.requestCharacters(params)) {
            is UseCaseResult.Success -> {
                val paginatedCharacter = result.data.first()
                if (paginatedCharacter.characters.isEmpty()) {
                    UseCaseResult.Failure(UseCaseResult.Reason.NoMoreCharacters)
                } else {
                    UseCaseResult.Success(paginatedCharacter)
                }
            }
            is UseCaseResult.Failure -> result
        }

    }
}