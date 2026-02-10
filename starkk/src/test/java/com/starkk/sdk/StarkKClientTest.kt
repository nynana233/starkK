package com.starkk.sdk

import com.starkk.sdk.models.Character
import com.starkk.sdk.models.House
import com.starkk.sdk.models.StarkKPageResult
import com.starkk.sdk.network.IceAndFireApi
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StarkKClientTest {

    @Test
    fun successfulCharacterFetchReturnsSuccess() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(
                Character(name = "Jon Snow", gender = "Male"),
                Character(name = "Daenerys Targaryen", gender = "Female"),
            )
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)

        assertIs<StarkKPageResult.Success<Character>>(result)
        assertEquals(2, result.page.items.size)
        assertEquals("Jon Snow", result.page.items[0].name)
        assertEquals("Daenerys Targaryen", result.page.items[1].name)
    }

    @Test
    fun paginationHeadersAreParsedIntoPage() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(Character(name = "Jon Snow")),
            nextPageUrl = "https://example.com/api/characters?page=2&pageSize=10"
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)

        assertIs<StarkKPageResult.Success<Character>>(result)
        assertTrue(result.page.hasNext)
        assertEquals(1, result.page.currentPage)
    }

    @Test
    fun httpErrorIsWrappedInFailure() = runTest {
        val mockApi = MockIceAndFireApi(httpError = true)
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)

        assertIs<StarkKPageResult.Failure>(result)
        assertIs<StarkKException.HttpError>(result.exception)
        assertEquals(500, (result.exception as StarkKException.HttpError).code)
    }

    @Test
    fun getCharactersByNameCallsCorrectEndpoint() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(Character(name = "Jon Snow"))
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharactersByName("Jon Snow", page = 1, pageSize = 10)

        assertIs<StarkKPageResult.Success<Character>>(result)
        assertEquals(1, result.page.items.size)
    }

    @Test
    fun getHousesReturnsHouses() = runTest {
        val mockApi = MockIceAndFireApi(
            houses = listOf(
                House(name = "House Stark", region = "The North"),
                House(name = "House Lannister", region = "The Westerlands"),
            )
        )
        val client = StarkKClient(mockApi)

        val result = client.getHouses(page = 1, pageSize = 10)

        assertIs<StarkKPageResult.Success<House>>(result)
        val houses = result.page.items
        assertEquals(2, houses.size)
        assertEquals("House Stark", houses[0].name)
    }

    @Test
    fun getBooksReturnsBooks() = runTest {
        val mockApi = MockIceAndFireApi(books = emptyList())
        val client = StarkKClient(mockApi)

        val result = client.getBooks(page = 1, pageSize = 10)

        assertIs<StarkKPageResult.Success<com.starkk.sdk.models.Book>>(result)
        assertTrue(result.page.items.isEmpty())
    }

    @Test
    fun emptyResponseBodyHandled() = runTest {
        val mockApi = MockIceAndFireApi(characters = emptyList())
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)

        assertIs<StarkKPageResult.Success<Character>>(result)
        assertTrue(result.page.items.isEmpty())
    }

    @Test
    fun onSuccessChainIsCalledForSuccess() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(Character(name = "Arya Stark"))
        )
        val client = StarkKClient(mockApi)

        var captured: List<Character>? = null
        client.getCharacters(page = 1, pageSize = 10)
            .onSuccess { page -> captured = page.items }
            .onFailure { /* should not be called */ }

        assertNotNull(captured)
        assertEquals("Arya Stark", captured!![0].name)
    }

    @Test
    fun onFailureChainIsCalledForError() = runTest {
        val mockApi = MockIceAndFireApi(httpError = true)
        val client = StarkKClient(mockApi)

        var captured: StarkKException? = null
        client.getCharacters(page = 1, pageSize = 10)
            .onSuccess { /* should not be called */ }
            .onFailure { e -> captured = e }

        assertNotNull(captured)
        assertIs<StarkKException.HttpError>(captured)
    }

    @Test
    fun nextCharactersReturnsNullWhenNoNextPage() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(Character(name = "Jon Snow")),
            nextPageUrl = null,
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)
        assertIs<StarkKPageResult.Success<Character>>(result)

        val next = client.nextCharacters(result.page)
        assertEquals(null, next)
    }

    @Test
    fun nextCharactersReturnsNextPage() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(Character(name = "Jon Snow")),
            nextPageUrl = "https://example.com/api/characters?page=2&pageSize=10",
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)
        assertIs<StarkKPageResult.Success<Character>>(result)

        val next = client.nextCharacters(result.page)
        assertNotNull(next)
        assertIs<StarkKPageResult.Success<Character>>(next)
    }

    @Test
    fun currentPageIsParsedFromLinkHeader() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(Character(name = "Jon Snow")),
            nextPageUrl = "https://example.com/api/characters?page=3&pageSize=10",
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 2, pageSize = 10)
        assertIs<StarkKPageResult.Success<Character>>(result)
        assertEquals(2, result.page.currentPage)
    }

    // Mock implementation
    private class MockIceAndFireApi(
        val characters: List<Character> = emptyList(),
        val houses: List<House> = emptyList(),
        val books: List<com.starkk.sdk.models.Book> = emptyList(),
        val nextPageUrl: String? = null,
        val httpError: Boolean = false,
    ) : IceAndFireApi {

        override suspend fun getCharacters(page: Int, pageSize: Int): Response<List<Character>> {
            return if (httpError) {
                Response.error(500, okhttp3.ResponseBody.create("application/json".toMediaType(), "Server Error"))
            } else {
                val headers = if (nextPageUrl != null) {
                    Headers.headersOf("Link", """<$nextPageUrl>; rel="next"""")
                } else {
                    Headers.headersOf()
                }
                Response.success(characters, headers)
            }
        }

        override suspend fun getCharactersByName(name: String, page: Int, pageSize: Int): Response<List<Character>> {
            return Response.success(characters)
        }

        override suspend fun getCharactersByQuery(
            name: String?, gender: String?, culture: String?, born: String?, died: String?,
            isAlive: Boolean?, page: Int, pageSize: Int
        ): Response<List<Character>> {
            return Response.success(characters)
        }

        override suspend fun getCharactersByUrl(url: String): Response<List<Character>> {
            return Response.success(characters)
        }

        override suspend fun getHouses(page: Int, pageSize: Int): Response<List<House>> {
            return Response.success(houses)
        }

        override suspend fun getHousesByName(name: String, page: Int, pageSize: Int): Response<List<House>> {
            return Response.success(houses)
        }

        override suspend fun getHousesByQuery(
            name: String?, region: String?, words: String?, hasWords: Boolean?, hasTitles: Boolean?,
            hasSeats: Boolean?, hasDiedOut: Boolean?, hasAncestralWeapons: Boolean?,
            page: Int, pageSize: Int
        ): Response<List<House>> {
            return Response.success(houses)
        }

        override suspend fun getHousesByUrl(url: String): Response<List<House>> {
            return Response.success(houses)
        }

        override suspend fun getBooks(page: Int, pageSize: Int): Response<List<com.starkk.sdk.models.Book>> {
            return Response.success(books)
        }

        override suspend fun getBooksByName(name: String, page: Int, pageSize: Int): Response<List<com.starkk.sdk.models.Book>> {
            return Response.success(books)
        }

        override suspend fun getBooksByUrl(url: String): Response<List<com.starkk.sdk.models.Book>> {
            return Response.success(books)
        }
    }
}
