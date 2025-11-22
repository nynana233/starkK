package com.starkk.sdk.network

import com.starkk.sdk.models.Book
import com.starkk.sdk.models.Character
import com.starkk.sdk.models.House
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Internal Retrofit service interface for "An API of Ice And Fire".
 *
 * All endpoints return [Response] wrappers so that HTTP headers
 * (particularly the `Link` header for pagination) can be inspected.
 */
internal interface IceAndFireApi {

    // Characters

    @GET("api/characters")
    suspend fun getCharacters(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): Response<List<Character>>

    @GET("api/characters")
    suspend fun getCharactersByName(
        @Query("name") name: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): Response<List<Character>>

    @GET("api/characters")
    suspend fun getCharactersByQuery(
        @Query("name") name: String? = null,
        @Query("gender") gender: String? = null,
        @Query("culture") culture: String? = null,
        @Query("born") born: String? = null,
        @Query("died") died: String? = null,
        @Query("isAlive") isAlive: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): Response<List<Character>>

    @GET
    suspend fun getCharactersByUrl(@Url url: String): Response<List<Character>>

    // Houses

    @GET("api/houses")
    suspend fun getHouses(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): Response<List<House>>

    @GET("api/houses")
    suspend fun getHousesByName(
        @Query("name") name: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): Response<List<House>>

    @GET("api/houses")
    suspend fun getHousesByQuery(
        @Query("name") name: String? = null,
        @Query("region") region: String? = null,
        @Query("words") words: String? = null,
        @Query("hasWords") hasWords: Boolean? = null,
        @Query("hasTitles") hasTitles: Boolean? = null,
        @Query("hasSeats") hasSeats: Boolean? = null,
        @Query("hasDiedOut") hasDiedOut: Boolean? = null,
        @Query("hasAncestralWeapons") hasAncestralWeapons: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): Response<List<House>>

    @GET
    suspend fun getHousesByUrl(@Url url: String): Response<List<House>>

    // Books

    @GET("api/books")
    suspend fun getBooks(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): Response<List<Book>>

    @GET("api/books")
    suspend fun getBooksByName(
        @Query("name") name: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
    ): Response<List<Book>>

    @GET
    suspend fun getBooksByUrl(@Url url: String): Response<List<Book>>
}

