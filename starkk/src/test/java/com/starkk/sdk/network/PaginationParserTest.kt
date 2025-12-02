package com.starkk.sdk.network

import com.starkk.sdk.models.PaginatedResult
import okhttp3.Headers
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaginationParserTest {

    @Test
    fun parseValidLinkHeaderWithAllRelations() {
        val linkHeader = """
            <https://anapioficeandfire.com/api/characters?page=2&pageSize=10>; rel="next",
            <https://anapioficeandfire.com/api/characters?page=1&pageSize=10>; rel="prev",
            <https://anapioficeandfire.com/api/characters?page=1&pageSize=10>; rel="first",
            <https://anapioficeandfire.com/api/characters?page=214&pageSize=10>; rel="last"
        """.trimIndent().replace("\n", "")

        val headers = Headers.headersOf("Link", linkHeader)
        val response = Response.success(listOf(1, 2, 3), headers)

        val result = PaginationParser.parse(response)

        assertEquals("https://anapioficeandfire.com/api/characters?page=2&pageSize=10", result.next)
        assertEquals("https://anapioficeandfire.com/api/characters?page=1&pageSize=10", result.prev)
        assertEquals("https://anapioficeandfire.com/api/characters?page=1&pageSize=10", result.first)
        assertEquals("https://anapioficeandfire.com/api/characters?page=214&pageSize=10", result.last)
    }

    @Test
    fun parsePartialLinkHeader() {
        val linkHeader = """
            <https://anapioficeandfire.com/api/characters?page=2&pageSize=10>; rel="next"
        """.trimIndent()

        val headers = Headers.headersOf("Link", linkHeader)
        val response = Response.success(listOf(1, 2, 3), headers)

        val result = PaginationParser.parse(response)

        assertEquals("https://anapioficeandfire.com/api/characters?page=2&pageSize=10", result.next)
        assertNull(result.prev)
        assertNull(result.first)
        assertNull(result.last)
    }

    @Test
    fun parseWithoutLinkHeader() {
        val headers = Headers.headersOf()
        val response = Response.success(listOf(1, 2, 3), headers)

        val result = PaginationParser.parse(response)

        assertTrue(result.data.size == 3)
        assertNull(result.next)
        assertNull(result.prev)
        assertNull(result.first)
        assertNull(result.last)
    }

    @Test
    fun parseWithNullBody() {
        val linkHeader = """
            <https://example.com/page=2>; rel="next"
        """.trimIndent()

        val headers = Headers.headersOf("Link", linkHeader)
        val response = Response.success<List<Int>>(null, headers)

        val result = PaginationParser.parse(response)

        assertTrue(result.data.isEmpty())
        assertEquals("https://example.com/page=2", result.next)
    }

    @Test
    fun parseLinkHeaderWithExtraWhitespace() {
        val linkHeader = """
            <https://example.com/page=2>  ;  rel="next"  ,
            <https://example.com/page=1>  ;  rel="prev"
        """.trimIndent().replace("\n", "")

        val headers = Headers.headersOf("Link", linkHeader)
        val response = Response.success(listOf("item1"), headers)

        val result = PaginationParser.parse(response)

        assertEquals("https://example.com/page=2", result.next)
        assertEquals("https://example.com/page=1", result.prev)
    }

    @Test
    fun parseEmptyDataListWithLinks() {
        val linkHeader = """
            <https://example.com/page=2>; rel="next"
        """.trimIndent()

        val headers = Headers.headersOf("Link", linkHeader)
        val response = Response.success(emptyList<Int>(), headers)

        val result = PaginationParser.parse(response)

        assertTrue(result.data.isEmpty())
        assertEquals("https://example.com/page=2", result.next)
    }

    @Test
    fun parseLinkHeaderWithQueryParameters() {
        val linkHeader = """
            <https://example.com/api?page=2&pageSize=50&filter=name>; rel="next"
        """.trimIndent()

        val headers = Headers.headersOf("Link", linkHeader)
        val response = Response.success(listOf("item"), headers)

        val result = PaginationParser.parse(response)

        assertEquals("https://example.com/api?page=2&pageSize=50&filter=name", result.next)
    }
}

