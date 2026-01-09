package com.renato.domain.model.character

import com.renato.domain.model.pagination.Pagination

data class PaginatedCharacter(
    val characters: List<Character>,
    val pagination: Pagination
)

data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val image: String
)
