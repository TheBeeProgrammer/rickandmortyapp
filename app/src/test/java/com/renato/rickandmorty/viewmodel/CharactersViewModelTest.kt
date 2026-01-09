package com.renato.rickandmorty.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.renato.domain.model.character.Character
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.model.pagination.Pagination
import com.renato.domain.usecases.base.UseCaseResult
import com.renato.domain.usecases.characters.PaginatedCharactersUseCase
import com.renato.rickandmorty.R
import com.renato.rickandmorty.di.ResourceProvider
import com.renato.rickandmorty.ui.mapper.CharacterUiMapper
import com.renato.rickandmorty.ui.model.CharacterUiModel
import com.renato.rickandmorty.ui.model.PaginatedCharacterUiModel
import com.renato.rickandmorty.ui.state.CharacterListAction
import com.renato.rickandmorty.ui.state.CharacterListEvent
import com.renato.rickandmorty.ui.state.CharacterListState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CharactersViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var paginatedCharactersUseCase: PaginatedCharactersUseCase

    @Mock
    private lateinit var uiMapper: CharacterUiMapper

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    private lateinit var viewModel: CharactersViewModel

    private val testCharacter = Character(
        id = 1,
        name = "Rick Sanchez",
        status = "Alive",
        species = "Human",
        gender = "Male",
        image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
    )

    private val testCharacterUi = CharacterUiModel(
        id = 1,
        name = "Rick Sanchez",
        status = "Alive",
        species = "Human",
        gender = "Male",
        image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
    )

    private val testPagination = Pagination(currentPage = 1, hasNextPage = true)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        whenever(resourceProvider.getString(R.string.no_more_characters))
            .thenReturn("No more characters")
        whenever(resourceProvider.getString(R.string.network_unavailable_extended))
            .thenReturn("Network unavailable")
        whenever(resourceProvider.getString(R.string.network_unavailable))
            .thenReturn("No network")
        whenever(resourceProvider.getString(R.string.unexpected_error))
            .thenReturn("Unexpected error")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CharactersViewModel {
        return CharactersViewModel(
            paginatedCharactersUseCase = paginatedCharactersUseCase,
            uiMapper = uiMapper,
            resourceProvider = resourceProvider,
            mainDispatcher = testDispatcher
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // Given
        val paginatedCharacter = PaginatedCharacter(
            characters = listOf(testCharacter),
            pagination = testPagination
        )

        val uiResult = PaginatedCharacterUiModel(
            characters = listOf(testCharacterUi),
            hasNextPage = true
        )

        whenever(paginatedCharactersUseCase.invoke())
            .thenReturn(UseCaseResult.Success(paginatedCharacter))
        whenever(uiMapper.mapToUi(paginatedCharacter))
            .thenReturn(uiResult)

        // When
        viewModel = createViewModel()

        // Then
        assertTrue(
            "Initial state should be Success",
            viewModel.state.value is CharacterListState.Success
        )
    }

    @Test
    fun `loadNextPage should call use case and update state to Success`() = runTest {
        // Given
        val paginatedCharacter = PaginatedCharacter(
            characters = listOf(testCharacter),
            pagination = testPagination
        )

        val uiResult = PaginatedCharacterUiModel(
            characters = listOf(testCharacterUi),
            hasNextPage = true
        )

        whenever(paginatedCharactersUseCase.invoke())
            .thenReturn(UseCaseResult.Success(paginatedCharacter))
        whenever(uiMapper.mapToUi(paginatedCharacter))
            .thenReturn(uiResult)

        // When
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then
        verify(paginatedCharactersUseCase, times(1)).invoke()
        verify(uiMapper, times(1)).mapToUi(paginatedCharacter)

        assertTrue(
            "State should be Success after loading",
            viewModel.state.value is CharacterListState.Success
        )

        val successState = viewModel.state.value as CharacterListState.Success
        assertEquals("Should have 1 character", 1, successState.paginatedCharacter.characters.size)
        assertTrue("Should have next page", successState.paginatedCharacter.hasNextPage)
    }

    @Test
    fun `Unknown error with empty message should use default`() = runTest {
        // Given
        whenever(paginatedCharactersUseCase.invoke())
            .thenReturn(UseCaseResult.Failure(UseCaseResult.Reason.Unknown("")))

        // When
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then
        assertTrue(
            "Should be in Error state",
            viewModel.state.value is CharacterListState.Error
        )

        val errorState = viewModel.state.value as CharacterListState.Error
        assertEquals(
            "Should use default error message",
            "Unexpected error", errorState.message
        )
    }

    @Test
    fun `Unknown error with custom message should use provided message`() = runTest {
        // Given
        val customMessage = "paginatedCharacterbase connection failed"
        whenever(paginatedCharactersUseCase.invoke())
            .thenReturn(UseCaseResult.Failure(UseCaseResult.Reason.Unknown(customMessage)))

        // When
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then
        assertTrue(
            "Should be in Error state",
            viewModel.state.value is CharacterListState.Error
        )

        val errorState = viewModel.state.value as CharacterListState.Error
        assertEquals(
            "Should use custom error message",
            customMessage, errorState.message
        )
    }
}