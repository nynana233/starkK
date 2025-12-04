package com.starkk.sdk

import com.starkk.sdk.models.Character
import com.starkk.sdk.models.House
import com.starkk.sdk.models.PaginatedResult
import com.starkk.sdk.network.IceAndFireApi
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StarkKClientTest {

    @Test
    fun successfulCharacterFetchReturnsResultSuccess() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(
                Character(name = "Jon Snow", gender = "Male"),
                Character(name = "Daenerys Targaryen", gender = "Female"),
            )
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)

        assertTrue(result.isSuccess)
        val paginatedResult = result.getOrNull()!!
        assertEquals(2, paginatedResult.data.size)
        assertEquals("Jon Snow", paginatedResult.data[0].name)
        assertEquals("Daenerys Targaryen", paginatedResult.data[1].name)
    }

    @Test
    fun paginationHeadersAreParsedIntoResult() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(Character(name = "Jon Snow")),
            nextPageUrl = "https://example.com/page=2"
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)

        assertTrue(result.isSuccess)
        val paginatedResult = result.getOrNull()!!
        assertEquals("https://example.com/page=2", paginatedResult.next)
    }

    @Test
    fun httpErrorIsWrappedInResultFailure() = runTest {
        val mockApi = MockIceAndFireApi(httpError = true)
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()!!
        assertIs<StarkKClient.HttpException>(exception)
        assertEquals(500, (exception as StarkKClient.HttpException).code)
    }

    @Test
    fun getCharactersByNameCallsCorrectEndpoint() = runTest {
        val mockApi = MockIceAndFireApi(
            characters = listOf(Character(name = "Jon Snow"))
        )
        val client = StarkKClient(mockApi)

        val result = client.getCharactersByName("Jon Snow", page = 1, pageSize = 10)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.data.size)
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

        assertTrue(result.isSuccess)
        val houses = result.getOrNull()!!.data
        assertEquals(2, houses.size)
        assertEquals("House Stark", houses[0].name)
    }

    @Test
    fun getBooksReturnsBooks() = runTest {
        val mockApi = MockIceAndFireApi(books = emptyList())
        val client = StarkKClient(mockApi)

        val result = client.getBooks(page = 1, pageSize = 10)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.data.isEmpty())
    }

    @Test
    fun emptyResponseBodyHandled() = runTest {
        val mockApi = MockIceAndFireApi(characters = emptyList())
        val client = StarkKClient(mockApi)

        val result = client.getCharacters(page = 1, pageSize = 10)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.data.isEmpty())
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




