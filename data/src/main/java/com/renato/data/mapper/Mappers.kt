package com.renato.data.mapper

import com.renato.data.api.entities.CharacterListResponse
import com.renato.data.api.entities.CharacterResponse
import com.renato.domain.model.character.Character
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.model.pagination.Pagination
import javax.inject.Inject

/**
 * Mapper class used to transform a [CharacterListResponse] API entity into a [PaginatedCharacter] domain model.
 *
 * This mapper handles the conversion of the raw API response data, including the list of character
 * entities and the associated pagination information, into a format suitable for the domain layer.
 */
class CharacterListResponseMapper @Inject constructor() :
    Mapper<CharacterListResponse, PaginatedCharacter> {

    /**
     * Converts this API CharacterResponse into a domain Character model.
     *
     * @return A Character whose properties are populated from this CharacterResponse.
     */
    private fun CharacterResponse.toDomain(): Character {
        return Character(
            id = id,
            name = name,
            status = status,
            species = species,
            gender = gender,
            image = image
        )
    }

    /**
     * Converts a CharacterListResponse into a PaginatedCharacter domain model.
     *
     * @param from The API response containing character results and pagination info.
     * @param currentPage The current page index; must not be null (a null value will cause a NullPointerException due to a non-null assertion).
     * @return A PaginatedCharacter containing mapped Character items and pagination metadata.
     */
    override fun map(
        from: CharacterListResponse,
        currentPage: Int?
    ): PaginatedCharacter {
        return PaginatedCharacter(
            characters = from.results.map { it.toDomain() },
            pagination = Pagination(
                currentPage = currentPage!!,
                hasNextPage = from.info.next != null
            )
        )
    }
}