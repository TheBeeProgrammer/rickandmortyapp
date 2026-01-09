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
import org.mockito.kotlin.verify
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
    }

    @Test
    fun `invoke first time returns success with first page`() = runTest {
        // Given
        val page1 = PaginatedCharacter(
            pagination = Pagination(currentPage = 1, hasNextPage = true),
            characters = listOf(Character(1, "Rick", "", "", "", ""))
        )
        whenever(repository.requestCharacters(1)).thenReturn(UseCaseResult.Success(flowOf(page1)))

        // When
        val result = useCase()

        // Then
        assertTrue(result is UseCaseResult.Success)
        val successResult = result as UseCaseResult.Success
        assertEquals(1, successResult.data.characters.size)
        assertEquals(1, successResult.data.characters[0].id)
    }

    @Test
    fun `invoke second time accumulates characters`() = runTest {
        // Given
        val page1 = PaginatedCharacter(
            pagination = Pagination(currentPage = 1, hasNextPage = true),
            characters = listOf(Character(1, "Rick", "", "", "", ""))
        )
        val page2 = PaginatedCharacter(
            pagination = Pagination(currentPage = 2, hasNextPage = true),
            characters = listOf(Character(2, "Morty", "", "", "", ""))
        )
        whenever(repository.requestCharacters(1)).thenReturn(UseCaseResult.Success(flowOf(page1)))
        whenever(repository.requestCharacters(2)).thenReturn(UseCaseResult.Success(flowOf(page2)))

        // When
        useCase() // First call
        val result = useCase() // Second call

        // Then
        assertTrue(result is UseCaseResult.Success)
        val successResult = result as UseCaseResult.Success
        assertEquals(2, successResult.data.characters.size)
        assertEquals(1, successResult.data.characters[0].id)
        assertEquals(2, successResult.data.characters[1].id)
    }

    @Test
    fun `invoke returns NoMoreCharacters after last page is reached`() = runTest {
        // Given
        val lastPage = PaginatedCharacter(
            pagination = Pagination(currentPage = 1, hasNextPage = false),
            characters = listOf(Character(1, "Rick", "", "", "", ""))
        )
        whenever(repository.requestCharacters(1)).thenReturn(UseCaseResult.Success(flowOf(lastPage)))

        // When
        useCase() // First call gets the last page
        val result = useCase() // Second call should fail

        // Then
        assertTrue(result is UseCaseResult.Failure)
        assertEquals(UseCaseResult.Reason.NoMoreCharacters, (result as UseCaseResult.Failure).reason)
    }

    @Test
    fun `reset clears state and restarts from page 1`() = runTest {
        // Given
        val page1 = PaginatedCharacter(
            pagination = Pagination(currentPage = 1, hasNextPage = true),
            characters = listOf(Character(1, "Rick", "", "", "", ""))
        )
        val page2 = PaginatedCharacter(
            pagination = Pagination(currentPage = 2, hasNextPage = true),
            characters = listOf(Character(2, "Morty", "", "", "", ""))
        )
        whenever(repository.requestCharacters(1)).thenReturn(UseCaseResult.Success(flowOf(page1)))
        whenever(repository.requestCharacters(2)).thenReturn(UseCaseResult.Success(flowOf(page2)))
        useCase() // Call once to move to page 2

        // When
        useCase.reset()
        val result = useCase() // Should now fetch page 1 again

        // Then
        verify(repository, org.mockito.kotlin.times(2)).requestCharacters(1)
        assertTrue(result is UseCaseResult.Success)
        val successResult = result as UseCaseResult.Success
        assertEquals(1, successResult.data.characters.size)
        assertEquals(1, successResult.data.characters[0].id)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        // Given
        val errorReason = UseCaseResult.Reason.NoInternet
        whenever(repository.requestCharacters(1)).thenReturn(UseCaseResult.Failure(errorReason))

        // When
        val result = useCase()

        // Then
        assertTrue(result is UseCaseResult.Failure)
        assertEquals(errorReason, (result as UseCaseResult.Failure).reason)
    }
}