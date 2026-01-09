package com.renato.rickandmorty.ui.model

import androidx.compose.runtime.Immutable

/**
 * UI-specific model for a character, optimized for Compose stability.
 *
 * @property id Unique identifier for the character.
 * @property name Character's full name.
 * @property status Character's survival status (e.g., "Alive", "Dead").
 * @property species Character's species.
 * @property gender Character's gender.
 * @property image URL to the character's image.
 */
@Immutable
data class CharacterUiModel(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val image: String
)

/**
 * UI-specific model for a paginated list of characters.
 *
 * @property characters The list of characters for the current page(s).
 * @property hasNextPage Whether there are more pages available to load.
 */
@Immutable
data class PaginatedCharacterUiModel(
    val characters: List<CharacterUiModel>,
    val hasNextPage: Boolean
)
