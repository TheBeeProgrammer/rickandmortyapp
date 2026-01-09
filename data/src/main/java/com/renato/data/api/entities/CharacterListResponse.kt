package com.renato.data.api.entities

data class CharacterListResponse(
    val info: PageInfo,
    val results: List<CharacterResponse>
)
