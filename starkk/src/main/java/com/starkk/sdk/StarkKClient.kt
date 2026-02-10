package com.starkk.sdk

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.starkk.sdk.models.Book
import com.starkk.sdk.models.Character
import com.starkk.sdk.models.House
import com.starkk.sdk.models.PaginatedResult
import com.starkk.sdk.models.StarkKPage
import com.starkk.sdk.models.StarkKPageResult
import com.starkk.sdk.network.IceAndFireApi
import com.starkk.sdk.network.PaginationParser
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Main entry-point for the StarkK SDK — a Kotlin-first Android SDK
 * for [An API of Ice And Fire](https://anapioficeandfire.com/).
 *
 * Obtain an instance via [Builder]:
 *
 * All public functions are **suspend** functions that return
 * [StarkKPageResult]<T> so callers can handle success/failure
 * idiomatically with `.onSuccess {}` / `.onFailure {}`.
 */
class StarkKClient internal constructor(
    private val api: IceAndFireApi,
) {

    // Characters

    /**
     * Fetch a page of characters.
     *
     * @param page 1-based page index (default `1`).
     * @param pageSize Number of results per page (default `10`, max `50`).
     */
    suspend fun getCharacters(
        page: Int = 1,
        pageSize: Int = 10,
    ): StarkKPageResult<Character> = safeApiCall(page) {
        api.getCharacters(page, pageSize)
    }

    /**
     * Search characters by exact name.
     */
    suspend fun getCharactersByName(
        name: String,
        page: Int = 1,
        pageSize: Int = 10,
    ): StarkKPageResult<Character> = safeApiCall(page) {
        api.getCharactersByName(name, page, pageSize)
    }

    /**
     * Search characters with flexible query parameters.
     */
    suspend fun getCharacters(
        name: String? = null,
        gender: String? = null,
        culture: String? = null,
        born: String? = null,
        died: String? = null,
        isAlive: Boolean? = null,
        page: Int = 1,
        pageSize: Int = 10,
    ): StarkKPageResult<Character> = safeApiCall(page) {
        api.getCharactersByQuery(name, gender, culture, born, died, isAlive, page, pageSize)
    }

    /**
     * Fetch the next page of characters from the given [page] cursor.
     *
     * @return The next [StarkKPageResult], or `null` if there is no next page.
     */
    suspend fun nextCharacters(
        page: StarkKPage<Character>,
    ): StarkKPageResult<Character>? {
        val url = page.nextUrl ?: return null
        return safeApiCall { api.getCharactersByUrl(url) }
    }

    /**
     * Fetch the previous page of characters from the given [page] cursor.
     *
     * @return The previous [StarkKPageResult], or `null` if there is no previous page.
     */
    suspend fun previousCharacters(
        page: StarkKPage<Character>,
    ): StarkKPageResult<Character>? {
        val url = page.prevUrl ?: return null
        return safeApiCall { api.getCharactersByUrl(url) }
    }

    /**
     * Fetch a page of characters using a full URL (typically from pagination).
     * @suppress Internal — use [nextCharacters] / [previousCharacters] instead.
     */
    internal suspend fun getCharactersByUrl(
        url: String,
    ): StarkKPageResult<Character> = safeApiCall {
        api.getCharactersByUrl(url)
    }

    // ── Houses ──────────────────────────────────────────────────

    /**
     * Fetch a page of houses.
     */
    suspend fun getHouses(
        page: Int = 1,
        pageSize: Int = 10,
    ): StarkKPageResult<House> = safeApiCall(page) {
        api.getHouses(page, pageSize)
    }

    /**
     * Search houses by exact name.
     */
    suspend fun getHousesByName(
        name: String,
        page: Int = 1,
        pageSize: Int = 10,
    ): StarkKPageResult<House> = safeApiCall(page) {
        api.getHousesByName(name, page, pageSize)
    }

    /**
     * Search houses with flexible query parameters.
     */
    suspend fun getHouses(
        name: String? = null,
        region: String? = null,
        words: String? = null,
        hasWords: Boolean? = null,
        hasTitles: Boolean? = null,
        hasSeats: Boolean? = null,
        hasDiedOut: Boolean? = null,
        hasAncestralWeapons: Boolean? = null,
        page: Int = 1,
        pageSize: Int = 10,
    ): StarkKPageResult<House> = safeApiCall(page) {
        api.getHousesByQuery(
            name, region, words, hasWords, hasTitles,
            hasSeats, hasDiedOut, hasAncestralWeapons, page, pageSize,
        )
    }

    /**
     * Fetch the next page of houses from the given [page] cursor.
     *
     * @return The next [StarkKPageResult], or `null` if there is no next page.
     */
    suspend fun nextHouses(
        page: StarkKPage<House>,
    ): StarkKPageResult<House>? {
        val url = page.nextUrl ?: return null
        return safeApiCall { api.getHousesByUrl(url) }
    }

    /**
     * Fetch the previous page of houses from the given [page] cursor.
     *
     * @return The previous [StarkKPageResult], or `null` if there is no previous page.
     */
    suspend fun previousHouses(
        page: StarkKPage<House>,
    ): StarkKPageResult<House>? {
        val url = page.prevUrl ?: return null
        return safeApiCall { api.getHousesByUrl(url) }
    }

    /**
     * Fetch a page of houses using a full URL (typically from pagination).
     * @suppress Internal — use [nextHouses] / [previousHouses] instead.
     */
    internal suspend fun getHousesByUrl(
        url: String,
    ): StarkKPageResult<House> = safeApiCall {
        api.getHousesByUrl(url)
    }

    // ── Books ───────────────────────────────────────────────────

    /**
     * Fetch a page of books.
     */
    suspend fun getBooks(
        page: Int = 1,
        pageSize: Int = 10,
    ): StarkKPageResult<Book> = safeApiCall(page) {
        api.getBooks(page, pageSize)
    }

    /**
     * Search books by exact name.
     */
    suspend fun getBooksByName(
        name: String,
        page: Int = 1,
        pageSize: Int = 10,
    ): StarkKPageResult<Book> = safeApiCall(page) {
        api.getBooksByName(name, page, pageSize)
    }

    /**
     * Fetch the next page of books from the given [page] cursor.
     *
     * @return The next [StarkKPageResult], or `null` if there is no next page.
     */
    suspend fun nextBooks(
        page: StarkKPage<Book>,
    ): StarkKPageResult<Book>? {
        val url = page.nextUrl ?: return null
        return safeApiCall { api.getBooksByUrl(url) }
    }

    /**
     * Fetch the previous page of books from the given [page] cursor.
     *
     * @return The previous [StarkKPageResult], or `null` if there is no previous page.
     */
    suspend fun previousBooks(
        page: StarkKPage<Book>,
    ): StarkKPageResult<Book>? {
        val url = page.prevUrl ?: return null
        return safeApiCall { api.getBooksByUrl(url) }
    }

    /**
     * Fetch a page of books using a full URL (typically from pagination).
     * @suppress Internal — use [nextBooks] / [previousBooks] instead.
     */
    internal suspend fun getBooksByUrl(
        url: String,
    ): StarkKPageResult<Book> = safeApiCall {
        api.getBooksByUrl(url)
    }

    // Internals

    /**
     * Wraps a Retrofit call in [StarkKPageResult], parsing the Link header
     * on success or wrapping the exception in [StarkKException] on failure.
     *
     * @param requestedPage The page number the caller requested (used as
     *                      fallback when the Link header cannot be parsed).
     */
    private inline fun <T> safeApiCall(
        requestedPage: Int = 1,
        call: () -> Response<List<T>>,
    ): StarkKPageResult<T> = try {
        val response = call()
        if (response.isSuccessful) {
            val paginated = PaginationParser.parse(response)
            StarkKPageResult.Success(StarkKPage.from(paginated, requestedPage))
        } else {
            StarkKPageResult.Failure(
                StarkKException.HttpError(response.code(), response.message()),
            )
        }
    } catch (e: IOException) {
        StarkKPageResult.Failure(StarkKException.NetworkError(e))
    } catch (e: Exception) {
        StarkKPageResult.Failure(StarkKException.UnknownError(e))
    }

    // Builder

    /**
     * Builder for [StarkKClient].
     */
    class Builder {
        private var baseUrl: String = BASE_URL
        private var okHttpClient: OkHttpClient? = null
        private var context: Context? = null
        private var loggingEnabled: Boolean = false
        private var connectTimeoutSeconds: Long = 30
        private var readTimeoutSeconds: Long = 30
        private var writeTimeoutSeconds: Long = 30
        private var cacheSizeBytes: Long = 10L * 1024 * 1024 // 10 MB

        /**
         * Override the default base URL.
         * Useful for testing against a mock server.
         */
        fun baseUrl(url: String) = apply { this.baseUrl = url }

        /**
         * Supply a fully-configured [OkHttpClient].
         * When set, [context], [enableLogging], and timeout settings are ignored.
         */
        fun okHttpClient(client: OkHttpClient) = apply { this.okHttpClient = client }

        /**
         * Provide an Android [Context] to enable disk caching.
         * The cache directory will be `context.cacheDir/starkk_http_cache`.
         */
        fun context(ctx: Context) = apply { this.context = ctx.applicationContext }

        /** Enable/disable OkHttp request & response logging. */
        fun enableLogging(enabled: Boolean) = apply { this.loggingEnabled = enabled }

        /** Set the connect timeout in seconds (default `30`). */
        fun connectTimeout(seconds: Long) = apply { this.connectTimeoutSeconds = seconds }

        /** Set the read timeout in seconds (default `30`). */
        fun readTimeout(seconds: Long) = apply { this.readTimeoutSeconds = seconds }

        /** Set the write timeout in seconds (default `30`). */
        fun writeTimeout(seconds: Long) = apply { this.writeTimeoutSeconds = seconds }

        /** Set the disk cache size in bytes (default `10 MB`). Only used when [context] is set. */
        fun cacheSize(bytes: Long) = apply { this.cacheSizeBytes = bytes }

        /**
         * Build the [StarkKClient] instance.
         */
        fun build(): StarkKClient {
            val client = okHttpClient ?: buildDefaultOkHttpClient()

            val contentType = "application/json".toMediaType()
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            val api = retrofit.create(IceAndFireApi::class.java)
            return StarkKClient(api)
        }

        private fun buildDefaultOkHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)

            // Disk cache
            context?.let { ctx ->
                val cacheDir = File(ctx.cacheDir, "starkk_http_cache")
                builder.cache(Cache(cacheDir, cacheSizeBytes))
            }

            // Logging
            if (loggingEnabled) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                builder.addInterceptor(loggingInterceptor)
            }

            return builder.build()
        }
    }

    companion object {
        internal const val BASE_URL = "https://anapioficeandfire.com/"
    }
}

