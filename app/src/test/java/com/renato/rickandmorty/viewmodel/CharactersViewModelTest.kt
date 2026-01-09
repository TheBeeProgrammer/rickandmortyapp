package com.renato.rickandmorty.viewmodel

import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.model.pagination.Pagination
import com.renato.domain.usecases.base.UseCaseResult
import com.renato.domain.usecases.characters.RequestNextPageOfCharacters
import com.renato.rickandmorty.ui.mapper.CharacterUiMapper
import com.renato.rickandmorty.ui.model.CharacterUiModel
import com.renato.rickandmorty.ui.model.PaginatedCharacterUiModel
import com.renato.rickandmorty.ui.state.CharacterListAction
import com.renato.rickandmorty.ui.state.CharacterListState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {

    private lateinit var viewModel: CharactersViewModel

    @Mock
    private lateinit var requestNextPageOfCharacters: RequestNextPageOfCharacters

    @Mock
    private lateinit var uiMapper: CharacterUiMapper

    private val testDispatcher = StandardTestDispatcher()

    private var closeable: AutoCloseable? = null

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable?.close()
    }

    private fun createViewModel() {
        viewModel = CharactersViewModel(
            requestNextPageOfCharacters,
            uiMapper,
            testDispatcher
        )
    }

    @Test
    fun `init calls loadNextPage and updates state to Success on success`() = runTest {
        // Given
        val domainResult = PaginatedCharacter(
            characters = emptyList(),
            pagination = Pagination(currentPage = 1, hasNextPage = true)
        )
        val uiResult = PaginatedCharacterUiModel(
            characters = listOf(mockCharacterUi1),
            hasNextPage = true
        )

        whenever(requestNextPageOfCharacters(1)).thenReturn(UseCaseResult.Success(domainResult))
        whenever(uiMapper.mapToUi(domainResult)).thenReturn(uiResult)

        // When
        createViewModel()
        advanceUntilIdle()

        // Then
        val expectedState = CharacterListState.Success(uiResult)
        assertEquals(expectedState, viewModel.state.value)
    }

    @Test
    fun `init updates state to Error on Unknown failure`() = runTest {
        // Given
        val errorMessage = "Something went wrong"
        whenever(requestNextPageOfCharacters(1)).thenReturn(
            UseCaseResult.Failure(UseCaseResult.Reason.Unknown(errorMessage))
        )

        // When
        createViewModel()
        advanceUntilIdle()

        // Then
        val expectedState = CharacterListState.Error(errorMessage)
        assertEquals(expectedState, viewModel.state.value)
    }

    @Test
    fun `LoadMoreCharacters appends characters when current state is Success`() = runTest {
        // Given
        val initialDomainResult = PaginatedCharacter(
            characters = emptyList(),
            pagination = Pagination(currentPage = 1, hasNextPage = true)
        )
        val initialUiResult = PaginatedCharacterUiModel(
            characters = listOf(mockCharacterUi1),
            hasNextPage = true
        )

        whenever(requestNextPageOfCharacters(1)).thenReturn(UseCaseResult.Success(initialDomainResult))
        whenever(uiMapper.mapToUi(initialDomainResult)).thenReturn(initialUiResult)

        createViewModel()
        advanceUntilIdle()

        val nextDomainResult = PaginatedCharacter(
            characters = emptyList(),
            pagination = Pagination(currentPage = 2, hasNextPage = true)
        )
        val nextUiResult = PaginatedCharacterUiModel(
            characters = listOf(mockCharacterUi2),
            hasNextPage = true
        )

        whenever(requestNextPageOfCharacters(2)).thenReturn(UseCaseResult.Success(nextDomainResult))
        whenever(uiMapper.mapToUi(nextDomainResult)).thenReturn(nextUiResult)

        // When
        viewModel.sendAction(CharacterListAction.LoadMoreCharacters)
        advanceUntilIdle()

        // Then
        val expectedCharacters = listOf(mockCharacterUi1, mockCharacterUi2)
        val actualState = viewModel.state.value as CharacterListState.Success
        assertEquals(expectedCharacters, actualState.paginatedCharacter.characters)
    }

    @Test
    fun `Retry reloads from page 1 when current state is Error`() = runTest {
        // Given
        whenever(requestNextPageOfCharacters(1)).thenReturn(
            UseCaseResult.Failure(UseCaseResult.Reason.NoInternet)
        )

        createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is CharacterListState.Error)

        whenever(requestNextPageOfCharacters(1)).thenReturn(
            UseCaseResult.Failure(UseCaseResult.Reason.NoMoreCharacters)
        )

        // When
        viewModel.sendAction(CharacterListAction.Retry)
        advanceUntilIdle()

        // Then
        verify(requestNextPageOfCharacters, org.mockito.kotlin.times(2)).invoke(1)
    }

    // Test data
    private val mockCharacterUi1 = CharacterUiModel(
        id = 1,
        name = "Rick Sanchez",
        status = "Alive",
        species = "Human",
        gender = "Male",
        image = "https://example.com/rick.png"
    )

    private val mockCharacterUi2 = CharacterUiModel(
        id = 2,
        name = "Morty Smith",
        status = "Alive",
        species = "Human",
        gender = "Male",
        image = "https://example.com/morty.png"
    )
}