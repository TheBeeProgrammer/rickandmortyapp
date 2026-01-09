import com.renato.domain.model.NetworkUnavailableException
import com.renato.domain.model.NoMoreCharactersException
import com.renato.domain.model.character.Character
import com.renato.domain.model.character.PaginatedCharacter
import com.renato.domain.model.pagination.Pagination
import com.renato.domain.repositories.CharacterRepository
import com.renato.domain.usecases.base.UseCaseResult
import com.renato.domain.usecases.characters.RequestNextPageOfCharacters
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.fail
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
    fun `invoke returns PaginatedCharacter when characters exist`() = runTest {
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
        assertEquals(paginatedCharacter, result)
        assertFalse(result.characters.isEmpty())
    }

    @Test(expected = NoMoreCharactersException::class)
    fun `invoke throws NoMoreCharactersException when character list is empty`() = runTest {
        // Given
        val page = 3
        val emptyPaginatedCharacter = PaginatedCharacter(
            pagination = Pagination(currentPage = page, hasNextPage = false),
            characters = emptyList()
        )

        whenever(repository.requestCharacters(page))
            .thenReturn(UseCaseResult.Success(flowOf(emptyPaginatedCharacter)))

        // When
        useCase.invoke(page)
    }

    @Test(expected = NetworkUnavailableException::class)
    fun `invoke throws NetworkUnavailableException when no internet`() = runTest {
        // Given
        val page = 1
        whenever(repository.requestCharacters(page))
            .thenReturn(UseCaseResult.Failure(UseCaseResult.Reason.NoInternet))

        // When
        useCase.invoke(page)
    }

    @Test
    fun `invoke throws Exception when unknown error occurs`() = runTest {
        // Given
        val page = 1
        val errorMessage = "Unknown error"
        whenever(repository.requestCharacters(page))
            .thenReturn(UseCaseResult.Failure(UseCaseResult.Reason.Unknown(errorMessage)))

        // When
        try {
            useCase.invoke(page)
            fail("Should have thrown an exception")
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
    }
}