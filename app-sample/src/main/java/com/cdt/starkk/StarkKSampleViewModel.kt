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
}