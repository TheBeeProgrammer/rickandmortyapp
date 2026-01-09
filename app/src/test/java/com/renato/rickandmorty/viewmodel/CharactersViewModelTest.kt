package com.renato.rickandmorty.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.renato.domain.model.NetworkUnavailableException
import com.renato.domain.model.character.Character
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.model.pagination.Pagination
import com.renato.domain.usecases.characters.RequestNextPageOfCharacters
import com.renato.rickandmorty.ui.state.CharacterListState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CharactersViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var requestNextPageOfCharacters: RequestNextPageOfCharacters

    private lateinit var viewModel: CharactersViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when ViewModel is created then it shows loading state and loads first page`() = runTest {
        val mockData = createMockPaginatedCharacter(page = 1, characterCount = 2)
        whenever(requestNextPageOfCharacters.invoke(1)).thenReturn(mockData)

        viewModel = CharactersViewModel(requestNextPageOfCharacters, testDispatcher)

        assertTrue(viewModel.state.value is CharacterListState.Loading)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is CharacterListState.Success)
        assertEquals(2, (state as CharacterListState.Success).paginatedCharacter.characters.size)
        verify(requestNextPageOfCharacters).invoke(1)
    }

    @Test
    fun `when network unavailable on first load then user sees error screen with retry option`() =
        runTest {
            whenever(requestNextPageOfCharacters.invoke(1)).thenAnswer {
                throw NetworkUnavailableException()
            }

            viewModel = CharactersViewModel(requestNextPageOfCharacters, testDispatcher)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state is CharacterListState.Error)
            assertEquals("Network unavailable", (state as CharacterListState.Error).message)
        }

    @Test
    fun `when unexpected error occurs on first load then user sees error screen`() = runTest {
        val errorMessage = "Server error 500"
        whenever(requestNextPageOfCharacters.invoke(1)).thenAnswer {
            throw RuntimeException(errorMessage)
        }

        viewModel = CharactersViewModel(requestNextPageOfCharacters, testDispatcher)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is CharacterListState.Error)
        assertEquals(errorMessage, (state as CharacterListState.Error).message)
    }

    @Test
    fun `when user retries after error then data loads successfully from beginning`() = runTest {
        val successData = createMockPaginatedCharacter(page = 1, characterCount = 2)
        whenever(requestNextPageOfCharacters.invoke(1))
            .thenAnswer { throw NetworkUnavailableException() }
            .thenReturn(successData)

        viewModel = CharactersViewModel(requestNextPageOfCharacters, testDispatcher)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is CharacterListState.Error)

        viewModel.retry()

        assertTrue(viewModel.state.value is CharacterListState.Loading)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is CharacterListState.Success)
        assertEquals(2, (state as CharacterListState.Success).paginatedCharacter.characters.size)
        verify(requestNextPageOfCharacters, times(2)).invoke(1)
    }

    @Test
    fun `when retry is called but no error exists then nothing happens`() = runTest {
        val page1Data = createMockPaginatedCharacter(page = 1, characterCount = 1)
        whenever(requestNextPageOfCharacters.invoke(1)).thenReturn(page1Data)

        viewModel = CharactersViewModel(requestNextPageOfCharacters, testDispatcher)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is CharacterListState.Success)

        viewModel.retry()
        advanceUntilIdle()

        verify(requestNextPageOfCharacters, times(1)).invoke(1)
    }

    private fun createMockPaginatedCharacter(
        page: Int,
        characterCount: Int,
        startId: Int = 1
    ): PaginatedCharacter {
        val characters = (startId until startId + characterCount).map { id ->
            Character(
                id = id,
                name = "Character $id",
                status = "Alive",
                species = "Human",
                gender = "Male",
                image = "image$id.jpg"
            )
        }

        return PaginatedCharacter(
            characters = characters,
            pagination = Pagination(currentPage = page, hasNextPage = true)
        )
    }
}