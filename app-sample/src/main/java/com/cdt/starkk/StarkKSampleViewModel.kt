package com.cdt.starkk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starkk.sdk.StarkKClient
import com.starkk.sdk.extensions.getHousesAsFlow
import com.starkk.sdk.models.Character
import com.starkk.sdk.models.House
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StarkKSampleViewModel : ViewModel() {
    private val client = StarkKClient.Builder()
        .enableLogging(true)
        .build()

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    private val _houses = MutableStateFlow<List<House>>(emptyList())
    val houses: StateFlow<List<House>> = _houses.asStateFlow()

    private val _singleResult = MutableStateFlow("")
    val singleResult: StateFlow<String> = _singleResult.asStateFlow()

    private val _isCharactersLoading = MutableStateFlow(false)
    val isCharactersLoading: StateFlow<Boolean> = _isCharactersLoading.asStateFlow()

    private val _isHousesLoading = MutableStateFlow(false)
    val isHousesLoading: StateFlow<Boolean> = _isHousesLoading.asStateFlow()

    private val _isSingleQueryLoading = MutableStateFlow(false)
    val isSingleQueryLoading: StateFlow<Boolean> = _isSingleQueryLoading.asStateFlow()

    private val _currentCharacterPage = MutableStateFlow(1)
    val currentCharacterPage: StateFlow<Int> = _currentCharacterPage.asStateFlow()

    private var characterPaginationUrls: Map<String, String?> = emptyMap()

    /**
     * Example 1: Using pagination with manual page control
     * Demonstrates fetching a specific page of characters
     */
    fun loadCharactersPage(page: Int, pageSize: Int) {
        viewModelScope.launch {
            _isCharactersLoading.value = true

            val result = client.getCharacters(page = page, pageSize = pageSize)

            result
                .onSuccess { paginated ->
                    _characters.value = paginated.data
                    _currentCharacterPage.value = page
                    characterPaginationUrls = mapOf(
                        "next" to paginated.next,
                        "prev" to paginated.prev
                    )
                }
                .onFailure { exception ->
                    _singleResult.value = "Error loading characters: ${exception.message}"
                }

            _isCharactersLoading.value = false
        }
    }

    /**
     * Navigate to next character page
     */
    fun nextCharacterPage(pageSize: Int) {
        val nextUrl = characterPaginationUrls["next"]
        if (nextUrl != null) {
            viewModelScope.launch {
                _isCharactersLoading.value = true

                val result = client.getCharactersByUrl(nextUrl)

                result
                    .onSuccess { paginated ->
                        _characters.value = paginated.data
                        _currentCharacterPage.value += 1
                        characterPaginationUrls = mapOf(
                            "next" to paginated.next,
                            "prev" to paginated.prev
                        )
                    }
                    .onFailure { exception ->
                        _singleResult.value = "Error loading characters: ${exception.message}"
                    }

                _isCharactersLoading.value = false
            }
        }
    }

    /**
     * Navigate to previous character page
     */
    fun previousCharacterPage(pageSize: Int) {
        val prevUrl = characterPaginationUrls["prev"]
        if (prevUrl != null) {
            viewModelScope.launch {
                _isCharactersLoading.value = true

                val result = client.getCharactersByUrl(prevUrl)

                result
                    .onSuccess { paginated ->
                        _characters.value = paginated.data
                        _currentCharacterPage.value -= 1
                        characterPaginationUrls = mapOf(
                            "next" to paginated.next,
                            "prev" to paginated.prev
                        )
                    }
                    .onFailure { exception ->
                        _singleResult.value = "Error loading characters: ${exception.message}"
                    }

                _isCharactersLoading.value = false
            }
        }
    }

    /**
     * Example 2: Using Flow for houses
     * Demonstrates auto-pagination through all houses using Flow
     */
    fun loadHousesFlow() {
        viewModelScope.launch {
            _isHousesLoading.value = true
            _houses.value = emptyList()

            try {
                client.getHousesAsFlow(pageSize = 50).collect { pageHouses ->
                    _houses.value = _houses.value + pageHouses
                }
            } catch (e: Exception) {
                _singleResult.value = "Error loading houses: ${e.message}"
            } finally {
                _isHousesLoading.value = false
            }
        }
    }

    /**
     * Example 3: Using Result<T> for single query
     * Demonstrates error handling with Kotlin Result type
     */
    fun queryCharacterByName(name: String) {
        viewModelScope.launch {
            _isSingleQueryLoading.value = true
            _singleResult.value = "Searching for '$name'..."

            val result = client.getCharactersByName(name, pageSize = 10)

            result
                .onSuccess { paginated ->
                    if (paginated.data.isEmpty()) {
                        _singleResult.value = "No characters found matching '$name'"
                    } else {
                        val character = paginated.data.first()
                        _singleResult.value = buildString {
                            append("✓ Found!\n\n")
                            append("Name: ${character.name}\n")
                            append("Gender: ${character.gender}\n")
                            append("Culture: ${character.culture}\n")
                            append("Born: ${character.born}\n")
                            if (character.titles.isNotEmpty()) {
                                append("Titles: ${character.titles.joinToString(", ")}\n")
                            }
                            if (character.aliases.isNotEmpty()) {
                                append("Aliases: ${character.aliases.joinToString(", ")}\n")
                            }
                        }
                    }
                }
                .onFailure { exception ->
                    _singleResult.value = "✗ Error: ${exception.message}"
                }

            _isSingleQueryLoading.value = false
        }
    }

    /**
     * Example 4: Query houses by name
     */
    fun queryHouseByName(name: String) {
        viewModelScope.launch {
            _isSingleQueryLoading.value = true
            _singleResult.value = "Searching for '$name'..."

            val result = client.getHousesByName(name, pageSize = 10)

            result
                .onSuccess { paginated ->
                    if (paginated.data.isEmpty()) {
                        _singleResult.value = "No houses found matching '$name'"
                    } else {
                        val house = paginated.data.first()
                        _singleResult.value = buildString {
                            append("✓ Found!\n\n")
                            append("Name: ${house.name}\n")
                            append("Region: ${house.region}\n")
                            if (house.words.isNotEmpty()) {
                                append("Words: \"${house.words}\"\n")
                            }
                            if (house.titles.isNotEmpty()) {
                                append("Titles: ${house.titles.take(2).joinToString(", ")}\n")
                            }
                        }
                    }
                }
                .onFailure { exception ->
                    _singleResult.value = "✗ Error: ${exception.message}"
                }

            _isSingleQueryLoading.value = false
        }
    }

    /**
     * Example 5: Query books with pagination info
     */
    fun queryBook() {
        viewModelScope.launch {
            _isSingleQueryLoading.value = true
            _singleResult.value = "Fetching books..."

            val result = client.getBooks(page = 1, pageSize = 5)

            result
                .onSuccess { paginated ->
                    if (paginated.data.isEmpty()) {
                        _singleResult.value = "No books found"
                    } else {
                        val book = paginated.data.random()
                        _singleResult.value = buildString {
                            append("✓ Found!\n\n")
                            append("Title: ${book.name}\n")
                            append("ISBN: ${book.isbn}\n")
                            append("Released: ${book.released}\n")
                            append("Pages: ${book.numberOfPages}\n")
                            append("Authors: ${book.authors.joinToString(", ")}\n")
                            append("\n📊 Pagination Info:\n")
                            append("Has Next: ${paginated.hasNextPage}\n")
                            append("Total Books (this page): ${paginated.data.size}")
                        }
                    }
                }
                .onFailure { exception ->
                    _singleResult.value = "✗ Error: ${exception.message}"
                }


            _isSingleQueryLoading.value = false
        }
    }
}