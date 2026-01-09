package com.renato.rickandmorty.viewmodel

import androidx.lifecycle.viewModelScope
import com.renato.domain.model.NetworkUnavailableException
import com.renato.domain.model.NoMoreCharactersException
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.usecases.characters.RequestNextPageOfCharacters
import com.renato.rickandmorty.ui.state.CharacterListAction
import com.renato.rickandmorty.ui.state.CharacterListEvent
import com.renato.rickandmorty.ui.state.CharacterListState
import com.renato.rickandmorty.viewmodel.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val requestNextPageOfCharacters: RequestNextPageOfCharacters
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
        }
    }

    private fun shouldLoadMore(): Boolean {
        return !isLoadingMore && hasMorePages && state.value is CharacterListState.Success
    }

    private fun loadNextPage() {
        if (isLoadingMore) return

        isLoadingMore = true

        viewModelScope.launch {
            try {
                val result = requestNextPageOfCharacters(currentPage)
                handleSuccessfulLoad(result)
                currentPage++
            } catch (e: NoMoreCharactersException) {
                handleNoMoreCharacters()
            } catch (e: NetworkUnavailableException) {
                handleNetworkError()
            } catch (e: Exception) {
                handleUnknownError(e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    private fun handleSuccessfulLoad(result: PaginatedCharacter) {
        updateState { existingState ->
            when (existingState) {
                is CharacterListState.Success -> {
                    val updatedCharacters = existingState.paginatedCharacter.characters + result.characters
                    val updatedPagination = result.copy(characters = updatedCharacters)
                    CharacterListState.Success(updatedPagination)
                }
                else -> CharacterListState.Success(result)
            }
        }
    }

    private fun handleNoMoreCharacters() {
        hasMorePages = false
        if (state.value is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError("No more characters to load"))
        }
    }

    private fun handleNetworkError() {
        val currentState = state.value

        if (currentState is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError("Network unavailable. Check your connection."))
        } else {
            updateState { CharacterListState.Error("Network unavailable") }
        }
    }

    private fun handleUnknownError(exception: Exception) {
        val errorMessage = exception.localizedMessage ?: "An unexpected error occurred"
        val currentState = state.value

        if (currentState is CharacterListState.Success) {
            sendEvent(CharacterListEvent.ShowError(errorMessage))
        } else {
            updateState { CharacterListState.Error(errorMessage) }
        }
    }

    fun retry() {
        if (state.value is CharacterListState.Error) {
            updateState { CharacterListState.Loading }
            currentPage = 1
            hasMorePages = true
            loadNextPage()
        }
    }
}