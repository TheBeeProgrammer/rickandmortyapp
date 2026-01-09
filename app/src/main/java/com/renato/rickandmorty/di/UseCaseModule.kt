package com.renato.rickandmorty.di

import com.renato.domain.usecases.characters.PaginatedCharactersUseCase
import com.renato.domain.usecases.characters.RequestNextPageOfCharacters
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindPaginatedCharactersUseCase(
        requestNextPageOfCharacters: RequestNextPageOfCharacters
    ): PaginatedCharactersUseCase
}
