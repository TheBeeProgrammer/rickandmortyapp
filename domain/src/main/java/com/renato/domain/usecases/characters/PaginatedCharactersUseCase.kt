package com.renato.domain.usecases.characters

import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.usecases.base.UseCaseResult

interface PaginatedCharactersUseCase {
    suspend operator fun invoke(): UseCaseResult<PaginatedCharacter>
    fun reset()
}
