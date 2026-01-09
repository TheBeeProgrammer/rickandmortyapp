package com.renato.rickandmorty.ui.state

import com.renato.rickandmorty.ui.model.PaginatedCharacterUiModel
import com.renato.rickandmorty.viewmodel.base.ViewAction
import com.renato.rickandmorty.viewmodel.base.ViewEvent
import com.renato.rickandmorty.viewmodel.base.ViewState

/**
 * Represents the actions that can be performed on the character list screen.
 * These actions are typically triggered by user interactions.
 */
sealed interface CharacterListAction : ViewAction {
    object LoadMoreCharacters : CharacterListAction
    object Retry : CharacterListAction
}

/**
 * Represents events that can be emitted by the character list feature.
 * These events are typically used to communicate one-time actions or results
 * from the ViewModel to the View (e.g., showing an error message).
 */
sealed interface CharacterListEvent : ViewEvent {
    data class ShowError(val message: String) : CharacterListEvent
}

/**
 * Represents the different states of the character list screen.
 *
 * @property Success The state when characters have been successfully loaded, containing the paginated UI data.
 * @property Loading The state when a character list request is currently in progress.
 * @property Error The state when an error occurs during the character loading process, containing an error message.
 */
sealed class CharacterListState : ViewState {
    data class Success(val paginatedCharacter: PaginatedCharacterUiModel) : CharacterListState()
    data class Error(val message: String) : CharacterListState()
    object Loading : CharacterListState()
}

