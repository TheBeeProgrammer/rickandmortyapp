package com.renato.data.api.entities

data class CharacterResponse(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val image: String
)
