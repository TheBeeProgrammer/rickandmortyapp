package com.clara.clarachallenge.ui.viewmodel.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * BaseViewModel is an abstract class that provides a foundation for managing UI state, events, and actions
 * in a structured and reactive manner using Kotlin coroutines and Flow.
 *
 * @param Action The type of actions that can be sent to the ViewModel.
 * @param State The type of state managed by the ViewModel.
 * @param Event The type of events emitted by the ViewModel.
 * @param defaultState The initial state of the ViewModel.
 */
abstract class BaseViewModel<Action : ViewAction, State : ViewState, Event : ViewEvent>(
    defaultState: State
) : ViewModel() {

    private val _state = MutableStateFlow(defaultState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _event.asSharedFlow()

    /**
     * Sends an action to the ViewModel for processing.
     *
     * @param action The action to be handled.
     */
    fun sendAction(action: Action) {
        handleAction(action)
    }

    /**
     * Updates the current state using a reducer function.
     *
     * @param reducer A function that takes the current state and returns the updated state.
     */
    protected fun updateState(reducer: (State) -> State) {
        _state.update { reducer(it) }
    }

    /**
     * Emits an event to the SharedFlow for one-time event handling.
     *
     * @param event The event to be emitted.
     */
    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    /**
     * Abstract method to handle actions sent to the ViewModel.
     *
     * @param action The action to be handled.
     */
    protected abstract fun handleAction(action: Action)
}
