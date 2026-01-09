package com.renato.rickandmorty.viewmodel

import androidx.lifecycle.viewModelScope
import com.renato.domain.MainDispatcher
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.usecases.base.UseCaseResult
import com.renato.domain.usecases.characters.RequestNextPageOfCharacters
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
 * It manages the pagination logic, handles user actions, and converts domain models
 * to UI models for presentation.
 *
 * @property requestNextPageOfCharacters Use case to fetch the next page of characters.
 * @property uiMapper Mapper to transform domain models to UI-specific models.
 * @property mainDispatcher Dispatcher to execute UI-related operations.
 */
@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val requestNextPageOfCharacters: RequestNextPageOfCharacters,
    private val uiMapper: CharacterUiMapper,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : BaseViewModel<CharacterListAction, CharacterListState, CharacterListEvent>(
    defaultState = CharacterListState.Loading
) {

    private var currentPage = 1
    private var isLoadingMore = false
    private var hasMorePages = true

    init {
        loadNextPage()
    }

    override fun handleAction(action: CharacterListAction) {
        when (action) {
            CharacterListAction.LoadMoreCharacters -> {
                if (shouldLoadMore()) {
                    loadNextPage()
                }
            }

            CharacterListAction.Retry -> {
                retry()
            }
        }
    }

    /**
     * Attempts to reload the character data after a failure.
     */
    private fun retry() {
        if (state.value is CharacterListState.Error) {
            updateState { CharacterListState.Loading }
            currentPage = 1
            hasMorePages = true
            loadNextPage()
        }
    }

    /**
     * Determines whether a new page of characters should be fetched.
     *
     * It checks three conditions:
     * 1. An existing load operation is not already in progress ([isLoadingMore]).
     * 2. There are more pages available to fetch ([hasMorePages]).
     * 3. The current state is [CharacterListState.Success], ensuring that the initial
     * load has completed before attempting to paginate.
     *
     * @return `true` if all conditions are met, `false` otherwise.
     */
    private fun shouldLoadMore(): Boolean {
        return !isLoadingMore && hasMorePages && state.value is CharacterListState.Success
    }

    /**
     * Requests the next page of characters and updates the ViewModel state.
     *
     * This function performs the following steps:
     * 1. Prevents duplicate requests by checking [isLoadingMore].
     * 2. Sets [isLoadingMore] to true and launches a coroutine on [mainDispatcher].
     * 3. Executes the [requestNextPageOfCharacters] use case using [currentPage].
     * 4. On success: Updates the state via [handleSuccessfulLoad] and increments [currentPage].
     * 5. On failure: Maps the [UseCaseResult.Reason] to specific error handlers:
     *    - [UseCaseResult.Reason.NoMoreCharacters] -> [handleNoMoreCharacters]
     *    - [UseCaseResult.Reason.NoInternet] -> [handleNetworkError]
     *    - [UseCaseResult.Reason.Unknown] -> [handleUnknownError]
     * 6. Ensures [isLoadingMore] is reset to false once the operation completes.
     */
    private fun loadNextPage() {
        if (isLoadingMore) return

        isLoadingMore = true

        viewModelScope.launch(mainDispatcher) {
            when (val result = requestNextPageOfCharacters(currentPage)) {
                is UseCaseResult.Success -> {
                    handleSuccessfulLoad(result.data)
                    currentPage++
                }

                is UseCaseResult.Failure -> {
                    when (result.reason) {
                        UseCaseResult.Reason.NoMoreCharacters -> handleNoMoreCharacters()
                        UseCaseResult.Reason.NoInternet -> handleNetworkError()
                        is UseCaseResult.Reason.Unknown -> handleUnknownError(Exception())
                    }
                }
            }
            isLoadingMore = false
        }
    }

    /**
     * Handles the successful retrieval of a [PaginatedCharacter] page.
     *
     * If the current state is [CharacterListState.Success], it appends the new characters to the
     * existing list and updates the pagination metadata. If the state is not [CharacterListState.Success]
     * (e.g., initial load), it sets the state to [CharacterListState.Success] with the provided result.
     *
     * @param result The [PaginatedCharacter] object containing the newly fetched characters and pagination info.
     */
    private fun handleSuccessfulLoad(result: PaginatedCharacter) {
        val uiResult = uiMapper.mapToUi(result)
        updateState { existingState ->
            when (existingState) {
                is CharacterListState.Success -> {
                    val updatedCharacters =
                        existingState.paginatedCharacter.characters + uiResult.characters
                    val updatedPagination = uiResult.copy(characters = updatedCharacters)
                    CharacterListState.Success(updatedPagination)
                }

                else -> CharacterListState.Success(uiResult)
            }
        }
    }

    /**
     * Handles the scenario where the end of the character list has been reached.
     *
     * Sets [hasMorePages] to false to prevent further pagination requests. If characters
     * are already being displayed (state is [CharacterListState.Success]), it triggers
     * a [CharacterListEvent.ShowError] to notify the user that no more data is available.
     */
    private fun handleNoMoreCharacters() {
        hasMorePages = false
        if (state.value is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError("No more characters to load"))
        }
    }

    /**
     * Handles network connectivity errors during data fetching.
     *
     * If the current state is [CharacterListState.Success], it implies a pagination error
     * and sends a [CharacterListEvent.ShowError] event to display a transient message.
     * Otherwise, it updates the view state to [CharacterListState.Error] for the initial load failure.
     */
    private fun handleNetworkError() {
        val currentState = state.value

        if (currentState is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError("Network unavailable. Check your connection."))
        } else {
            updateState { CharacterListState.Error("Network unavailable") }
        }
    }

    /**
     * Handles unexpected exceptions during character data fetching.
     *
     * If the current state is [CharacterListState.Success], it triggers a [CharacterListEvent.ShowError]
     * Otherwise, it updates the view state to [CharacterListState.Error].
     *
     * @param exception The [Exception] that occurred during the network request or data processing.
     */
    private fun handleUnknownError(exception: Exception) {
        val errorMessage = exception.localizedMessage ?: "An unexpected error occurred"
        val currentState = state.value

        if (currentState is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError(errorMessage))
        } else {
            updateState { CharacterListState.Error(errorMessage) }
        }
    }
}