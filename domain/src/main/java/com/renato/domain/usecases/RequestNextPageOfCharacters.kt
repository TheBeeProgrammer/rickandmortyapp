package com.renato.domain.usecases

import com.renato.domain.model.NoMoreCharactersException
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.repositories.CharacterRepository
import jakarta.inject.Inject

class RequestNextPageOfCharacters @Inject constructor(private val repository: CharacterRepository) {

    suspend operator fun invoke(pageToLoad: Int): PaginatedCharacter {
        val paginatedCharacter = repository.requestCharacters(pageToLoad)

        if (paginatedCharacter.characters.isEmpty()) {
            throw NoMoreCharactersException()
        }

        return paginatedCharacter
    }
}
