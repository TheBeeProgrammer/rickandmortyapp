package com.renato.rickandmorty.viewmodel

import androidx.lifecycle.viewModelScope
import com.renato.domain.qualifier.MainDispatcher
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.usecases.base.UseCaseResult
import com.renato.domain.usecases.characters.PaginatedCharactersUseCase
import com.renato.rickandmorty.R
import com.renato.rickandmorty.di.ResourceProvider
import com.renato.rickandmorty.ui.mapper.CharacterUiMapper
import com.renato.rickandmorty.ui.state.CharacterListAction
import com.renato.rickandmorty.ui.state.CharacterListEvent
import com.renato.rickandmorty.ui.state.CharacterListState
import com.renato.rickandmorty.viewmodel.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the character list screen.
 *
 * It delegates pagination logic to a use case, handles user actions,
 * and converts domain models to UI models for presentation.
 *
 * @property paginatedCharactersUseCase Use case to fetch pages of characters.
 * @property uiMapper Mapper to transform domain models to UI-specific models.
 * @property mainDispatcher Dispatcher to execute UI-related operations.
 */
@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val paginatedCharactersUseCase: PaginatedCharactersUseCase,
    private val uiMapper: CharacterUiMapper,
    private val resourceProvider: ResourceProvider,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : BaseViewModel<CharacterListAction, CharacterListState, CharacterListEvent>(
    defaultState = CharacterListState.Loading
) {

    private var isLoading = false

    init {
        loadNextPage()
    }

    override fun handleAction(action: CharacterListAction) {
        when (action) {
            CharacterListAction.LoadMoreCharacters -> loadNextPage()
            CharacterListAction.Retry -> retry()
        }
    }

    /**
     * Attempts to reload the character data after a failure.
     */
    private fun retry() {
        if (state.value is CharacterListState.Error) {
            updateState { CharacterListState.Loading }
            paginatedCharactersUseCase.reset()
            loadNextPage()
        }
    }

    /**
     * Requests the next page of characters and updates the ViewModel state.
     */
    private fun loadNextPage() {
        if (isLoading) return

        isLoading = true

        viewModelScope.launch(mainDispatcher) {
            when (val result = paginatedCharactersUseCase()) {
                is UseCaseResult.Success -> handleSuccessfulLoad(result.data)
                is UseCaseResult.Failure -> {
                    when (val reason = result.reason) {
                        UseCaseResult.Reason.NoMoreCharacters -> handleNoMoreCharacters()
                        UseCaseResult.Reason.NoInternet -> handleNetworkError()
                        is UseCaseResult.Reason.Unknown -> handleUnknownError(reason.message)
                    }
                }
            }
            isLoading = false
        }
    }

    /**
     * Handles the successful retrieval of a [PaginatedCharacter] page.
     */
    private fun handleSuccessfulLoad(result: PaginatedCharacter) {
        val uiResult = uiMapper.mapToUi(result)
        updateState { CharacterListState.Success(uiResult) }
    }

    /**
     * Handles the scenario where the end of the character list has been reached.
     */
    private fun handleNoMoreCharacters() {
        if (state.value is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError(resourceProvider.getString(R.string.no_more_characters)))
        }
    }

    /**
     * Handles network connectivity errors during data fetching.
     */
    private fun handleNetworkError() {
        if (state.value is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError(resourceProvider.getString(R.string.network_unavailable_extended)))
        } else {
            updateState { CharacterListState.Error(resourceProvider.getString(R.string.network_unavailable)) }
        }
    }

    /**
     * Handles unexpected or unknown errors during data fetching.
     */
    private fun handleUnknownError(message: String) {
        val errorMessage = message.ifEmpty { resourceProvider.getString(R.string.unexpected_error) }

        if (state.value is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError(errorMessage))
        } else {
            updateState { CharacterListState.Error(errorMessage) }
        }
    }
}