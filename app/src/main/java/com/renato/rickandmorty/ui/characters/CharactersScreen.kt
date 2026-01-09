package com.renato.rickandmorty.ui.characters

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.renato.logger.Logger
import com.renato.rickandmorty.R
import com.renato.rickandmorty.ui.components.CharacterItem
import com.renato.rickandmorty.ui.components.ErrorScreen
import com.renato.rickandmorty.ui.components.LoadingScreen
import com.renato.rickandmorty.ui.components.PaginationLoadingItem
import com.renato.rickandmorty.ui.model.CharacterUiModel
import com.renato.rickandmorty.ui.model.PaginatedCharacterUiModel
import com.renato.rickandmorty.ui.state.CharacterListAction
import com.renato.rickandmorty.ui.state.CharacterListEvent
import com.renato.rickandmorty.ui.state.CharacterListState
import com.renato.rickandmorty.ui.theme.RickandmortyTheme
import com.renato.rickandmorty.viewmodel.CharactersViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

/**
 * The main screen that manages the character list.
 *
 * This composable is responsible for collecting the ViewModel state and events,
 * and orchestrating the different UI states (Loading, Success, Error).
 *
 * @param viewModel The ViewModel that manages the screen's state and logic.
 * @param modifier The modifier to be applied to the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersScreen(
    viewModel: CharactersViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CharactersScreen(
        state = state,
        events = viewModel.events,
        onAction = { viewModel.sendAction(it) },
        modifier = modifier
    )
}

/**
 * The stateless version of the [CharactersScreen] that displays the list of characters.
 *
 * This composable handles the layout of the screen, including the [TopAppBar], [SnackbarHost]
 * for error messaging, and the main content area. It also reacts to [CharacterListEvent]s
 * emitted by the [events] flow.
 *
 * @param state The current [CharacterListState] to be displayed.
 * @param events A [Flow] of [CharacterListEvent] used to handle one-time UI events like showing snackbars.
 * @param onAction A callback function to handle user or UI actions defined in [CharacterListAction].
 * @param modifier The [Modifier] to be applied to the layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersScreen(
    state: CharacterListState,
    events: Flow<CharacterListEvent>,
    onAction: (CharacterListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(events) {
        events.collectLatest { event ->
            when (event) {
                is CharacterListEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                    Logger.e(Exception(event.message), event.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.characters_screen_title)) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            CharactersContent(
                state = state,
                onLoadMore = { onAction(CharacterListAction.LoadMoreCharacters) },
                onRetry = { onAction(CharacterListAction.Retry) }
            )
        }
    }
}

/**
 * Displays the appropriate content based on the current character list state.
 *
 * @param state The current state of the character list.
 * @param onLoadMore Callback to trigger loading the next page of characters.
 * @param onRetry Callback to retry loading character data after an error.
 * @param modifier The modifier to be applied to the content box.
 */
@Composable
fun CharactersContent(
    state: CharacterListState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is CharacterListState.Loading -> {
            LoadingScreen(modifier = modifier)
        }

        is CharacterListState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = onRetry,
                modifier = modifier
            )
        }

        is CharacterListState.Success -> {
            CharacterList(
                paginatedCharacter = state.paginatedCharacter,
                onLoadMore = onLoadMore,
                modifier = modifier
            )
        }
    }
}

/**
 * Displays a paginated list of characters in a LazyColumn.
 *
 * This composable handles infinite scrolling by triggering [onLoadMore]
 * when the user scrolls near the end of the list.
 *
 * @param paginatedCharacter The UI model containing the list of characters and pagination status.
 * @param onLoadMore Callback to be invoked when more characters need to be loaded.
 * @param modifier The modifier to be applied to the LazyColumn.
 */
@Composable
fun CharacterList(
    paginatedCharacter: PaginatedCharacterUiModel,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = paginatedCharacter.characters,
            key = { it.id }
        ) { character ->
            CharacterItem(character = character)
        }

        if (paginatedCharacter.hasNextPage) {
            item {
                PaginationLoadingItem()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CharactersScreenPreview() {
    RickandmortyTheme {
        CharactersScreen(
            state = CharacterListState.Success(
                paginatedCharacter = PaginatedCharacterUiModel(
                    characters = listOf(
                        CharacterUiModel(
                            1,
                            "Rick Sanchez",
                            "Alive",
                            "Human",
                            "Male",
                            ""
                        ),
                        CharacterUiModel(
                            2,
                            "Morty Smith",
                            "Alive",
                            "Human",
                            "Male",
                            ""
                        ),
                        CharacterUiModel(
                            3,
                            "Summer Smith",
                            "Alive",
                            "Human",
                            "Female",
                            ""
                        ),
                        CharacterUiModel(
                            4,
                            "Beth Smith",
                            "Alive",
                            "Human",
                            "Female",
                            ""
                        ),
                        CharacterUiModel(
                            5,
                            "Jerry Smith",
                            "Alive",
                            "Human",
                            "Male",
                            ""
                        )
                    ),
                    hasNextPage = true
                )
            ),
            events = emptyFlow(),
            onAction = {}
        )
    }
}
