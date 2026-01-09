package com.renato.data.api.entities

data class PageInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)