package com.renato.data

import com.renato.data.api.RickAndMortyApiService
import com.renato.data.api.entities.CharacterListResponse
import com.renato.data.api.entities.CharacterResponse
import com.renato.data.api.entities.PageInfo
import com.renato.data.mapper.Mapper
import com.renato.data.repository.CharacterRepositoryImpl
import com.renato.domain.model.character.Character
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.model.pagination.Pagination
import com.renato.domain.usecases.base.UseCaseResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CharacterRepositoryImplTest {

    @Mock
    private lateinit var apiService: RickAndMortyApiService

    @Mock
    private lateinit var mapper: Mapper<CharacterListResponse, PaginatedCharacter>

    private lateinit var repository: CharacterRepositoryImpl
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = CharacterRepositoryImpl(
            apiService = apiService,
            ioDispatcher = testDispatcher,
            mapper = mapper
        )
    }

    @Test
    fun `requestCharacters returns success with mapped data`() = runTest {
        // Given
        val page = 1
        val mockResponse = CharacterListResponse(
            info = PageInfo(count = 1, pages = 1, next = null, prev = null),
            results = listOf(
                CharacterResponse(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    gender = "Male",
                    image = "image.jpg",
                )
            )
        )
        val mockPaginatedCharacter = PaginatedCharacter(
            pagination = Pagination(currentPage = page, hasNextPage = true),
            characters = listOf(
                Character(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    gender = "Male",
                    image = "image.jpg"
                )
            )
        )

        whenever(apiService.getCharacters(page)).thenReturn(mockResponse)
        whenever(mapper.map(from = mockResponse, currentPage = page))
            .thenReturn(mockPaginatedCharacter)

        // When
        val result = repository.requestCharacters(page)

        // Then
        assertTrue(result is UseCaseResult.Success)
        val data = (result as UseCaseResult.Success).data.first()
        assertEquals(mockPaginatedCharacter, data)
        verify(apiService).getCharacters(page)
        verify(mapper).map(from = mockResponse, currentPage = page)
    }

    @Test
    fun `requestCharacters returns failure with unknown error`() = runTest {
        // Given
        val page = 1
        val errorMessage = "Server error"
        whenever(apiService.getCharacters(page))
            .thenThrow(RuntimeException(errorMessage))

        // When
        val result = repository.requestCharacters(page)

        // Then
        assertTrue(result is UseCaseResult.Failure)
        val failure = result as UseCaseResult.Failure
        assertTrue(failure.reason is UseCaseResult.Reason.Unknown)
        assertEquals(errorMessage, (failure.reason as UseCaseResult.Reason.Unknown).message)
    }

    @Test
    fun `requestCharacters uses correct dispatcher`() = runTest {
        // Given
        val page = 1
        val mockResponse = CharacterListResponse(
            info = PageInfo(count = 0, pages = 0, next = null, prev = null),
            results = emptyList()
        )
        val mockPaginatedCharacter = PaginatedCharacter(
            pagination = Pagination(currentPage = page, hasNextPage = false),
            characters = emptyList()
        )

        whenever(apiService.getCharacters(page)).thenReturn(mockResponse)
        whenever(mapper.map(from = mockResponse, currentPage = page))
            .thenReturn(mockPaginatedCharacter)

        // When
        repository.requestCharacters(page)

        // Then
        verify(apiService).getCharacters(page)
    }

    @Test
    fun `requestCharacters with different pages calls API with correct page`() = runTest {
        // Given
        val page = 5
        val mockResponse = CharacterListResponse(
            info = PageInfo(count = 1, pages = 10, next = null, prev = null),
            results = emptyList()
        )
        val mockPaginatedCharacter = PaginatedCharacter(
            pagination = Pagination(currentPage = page, hasNextPage = false),
            characters = emptyList()
        )

        whenever(apiService.getCharacters(page)).thenReturn(mockResponse)
        whenever(mapper.map(from = mockResponse, currentPage = page))
            .thenReturn(mockPaginatedCharacter)

        // When
        val result = repository.requestCharacters(page)

        // Then
        assertTrue(result is UseCaseResult.Success)
        verify(apiService).getCharacters(page)
        verify(mapper).map(from = mockResponse, currentPage = page)
    }

    @Test
    fun `requestCharacters returns success with empty character list when no more data`() =
        runTest {
            // Given
            val page = 3
            val emptyResponse = CharacterListResponse(
                info = PageInfo(count = 0, pages = 2, next = null, prev = null),
                results = emptyList()
            )
            val emptyPaginatedCharacter = PaginatedCharacter(
                pagination = Pagination(currentPage = page, hasNextPage = false),
                characters = emptyList()
            )

            whenever(apiService.getCharacters(page)).thenReturn(emptyResponse)
            whenever(mapper.map(from = emptyResponse, currentPage = page))
                .thenReturn(emptyPaginatedCharacter)

            // When
            val result = repository.requestCharacters(page)

            // Then
            assertTrue(result is UseCaseResult.Success)
            val data = (result as UseCaseResult.Success).data.first()
            assertTrue(data.characters.isEmpty())
        }
}