package com.starkk.sdk.network

import com.starkk.sdk.models.PaginatedResult
import retrofit2.Response

/**
 * Internal utility that parses RFC 5988 `Link` headers from an HTTP response
 * and wraps the body together with pagination URLs into a [PaginatedResult].
 *
 * Example `Link` header value:
 * ```
 * <https://anapioficeandfire.com/api/characters?page=2&pageSize=10>; rel="next",
 * <https://anapioficeandfire.com/api/characters?page=1&pageSize=10>; rel="prev",
 * <https://anapioficeandfire.com/api/characters?page=1&pageSize=10>; rel="first",
 * <https://anapioficeandfire.com/api/characters?page=214&pageSize=10>; rel="last"
 * ```
 */
internal object PaginationParser {

    /**
     * Regex that captures each `<url>; rel="relation"` segment.
     * - Group 1: the URL between `<` and `>`
     * - Group 2: the rel value between quotes
     */
    private val LINK_PATTERN = Regex("""<([^>]+)>\s*;\s*rel="([^"]+)""")

    /**
     * Parses the `Link` header from the given Retrofit [Response] and returns
     * a [PaginatedResult] containing the response body and pagination URLs.
     *
     * If the response body is `null`, an empty list is used as fallback.
     *
     * @param T The element type of the response body list.
     * @param response A successful Retrofit [Response] wrapping a `List<T>`.
     * @return A fully-populated [PaginatedResult].
     */
    fun <T> parse(response: Response<List<T>>): PaginatedResult<T> {
        val data = response.body() ?: emptyList()
        val linkHeader = response.headers()["Link"] ?: return PaginatedResult(data = data)

        val links = mutableMapOf<String, String>()
        LINK_PATTERN.findAll(linkHeader).forEach { match ->
            val url = match.groupValues[1]
            val rel = match.groupValues[2]
            links[rel] = url
        }

        return PaginatedResult(
            data = data,
            next = links["next"],
            prev = links["prev"],
            first = links["first"],
            last = links["last"],
        )
    }
}

