package com.renato.rickandmorty.ui.mapper

import com.renato.domain.model.character.Character
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.rickandmorty.ui.model.CharacterUiModel
import com.renato.rickandmorty.ui.model.PaginatedCharacterUiModel
import javax.inject.Inject

/**
 * Mapper class responsible for converting domain models to UI-specific models.
 *
 * This provides a layer of separation between the business logic and the presentation layer,
 * allowing UI models to be marked as @Immutable for Compose optimizations.
 */
class CharacterUiMapper @Inject constructor() {

    /**
     * Maps a single domain [Character] to its UI representation.
     *
     * @param domain The domain model to convert.
     * @return A stable [CharacterUiModel].
     */
    fun mapToUi(domain: Character): CharacterUiModel {
        return CharacterUiModel(
            id = domain.id,
            name = domain.name,
            status = domain.status,
            species = domain.species,
            gender = domain.gender,
            image = domain.image
        )
    }

    /**
     * Maps a [PaginatedCharacter] domain model to its UI representation.
     *
     * @param domain The domain model containing characters and pagination info.
     * @return A stable [PaginatedCharacterUiModel].
     */
    fun mapToUi(domain: PaginatedCharacter): PaginatedCharacterUiModel {
        return PaginatedCharacterUiModel(
            characters = domain.characters.map { mapToUi(it) },
            hasNextPage = domain.pagination.hasNextPage
        )
    }
}
