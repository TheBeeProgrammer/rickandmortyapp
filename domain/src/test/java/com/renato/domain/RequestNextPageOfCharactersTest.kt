package com.renato.domain

import com.renato.domain.model.character.Character
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.model.pagination.Pagination
import com.renato.domain.repositories.CharacterRepository
import com.renato.domain.usecases.base.UseCaseResult
import com.renato.domain.usecases.characters.RequestNextPageOfCharacters
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RequestNextPageOfCharactersTest {

    @Mock
    private lateinit var repository: CharacterRepository

    private lateinit var useCase: RequestNextPageOfCharacters
    private var closeable: AutoCloseable? = null

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        useCase = RequestNextPageOfCharacters(repository)
    }

    @After
    fun tearDown() {
        closeable?.close()
        closeable = null
    }

    @Test
    fun `invoke returns Success with PaginatedCharacter when characters exist`() = runTest {
        // Given
        val page = 1
        val paginatedCharacter = PaginatedCharacter(
            pagination = Pagination(currentPage = page, hasNextPage = true),
            characters = listOf(
                Character(1, "Rick", "Alive", "Human", "Male", "image.jpg")
            )
        )
        whenever(repository.requestCharacters(page))
            .thenReturn(UseCaseResult.Success(flowOf(paginatedCharacter)))

        // When
        val result = useCase.invoke(page)

        // Then
        assertTrue(result is UseCaseResult.Success)
        assertEquals(paginatedCharacter, (result as UseCaseResult.Success).data)
    }

    @Test
    fun `invoke returns NoMoreCharacters failure when repository returns empty character list`() = runTest {
        // Given
        val page = 3
        val emptyPaginatedCharacter = PaginatedCharacter(
            pagination = Pagination(currentPage = page, hasNextPage = false),
            characters = emptyList()
        )
        whenever(repository.requestCharacters(page))
            .thenReturn(UseCaseResult.Success(flowOf(emptyPaginatedCharacter)))

        // When
        val result = useCase.invoke(page)

        // Then
        assertTrue(result is UseCaseResult.Failure)
        assertEquals(UseCaseResult.Reason.NoMoreCharacters, (result as UseCaseResult.Failure).reason)
    }

    @Test
    fun `invoke returns NoInternet failure when repository returns NoInternet`() = runTest {
        // Given
        val page = 1
        whenever(repository.requestCharacters(page))
            .thenReturn(UseCaseResult.Failure(UseCaseResult.Reason.NoInternet))

        // When
        val result = useCase.invoke(page)

        // Then
        assertTrue(result is UseCaseResult.Failure)
        assertEquals(UseCaseResult.Reason.NoInternet, (result as UseCaseResult.Failure).reason)
    }

    @Test
    fun `invoke returns Unknown failure when repository returns Unknown`() = runTest {
        // Given
        val page = 1
        val errorMessage = "Unknown error"
        whenever(repository.requestCharacters(page))
            .thenReturn(UseCaseResult.Failure(UseCaseResult.Reason.Unknown(errorMessage)))

        // When
        val result = useCase.invoke(page)

        // Then
        assertTrue(result is UseCaseResult.Failure)
        val reason = (result as UseCaseResult.Failure).reason
        assertTrue(reason is UseCaseResult.Reason.Unknown)
        assertEquals(errorMessage, (reason as UseCaseResult.Reason.Unknown).message)
    }
}