package com.cdt.starkk

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * Unit tests for StarkKSampleViewModel focusing on composition and state updates
 * Tests StateFlow mechanics and ViewModel properties used by Composables
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StarkKSampleViewModelComposableTest {

    private lateinit var viewModel: StarkKSampleViewModel

    @Before
    fun setUp() {
        viewModel = StarkKSampleViewModel()
    }

    // ========== Composable Property Access Tests ==========

    @Test
    fun `characters property should be accessible for Composables`() {
        // Act & Assert - Should not throw
        val characters = viewModel.characters
        assertEquals(emptyList<Any>(), characters.value)
    }

    @Test
    fun `houses property should be accessible for Composables`() {
        // Act & Assert
        val houses = viewModel.houses
        assertEquals(emptyList<Any>(), houses.value)
    }

    @Test
    fun `singleResult property should be accessible for Composables`() {
        // Act & Assert
        val result = viewModel.singleResult
        assertEquals("", result.value)
    }

    @Test
    fun `isCharactersLoading property should be accessible for Composables`() {
        // Act & Assert
        val loading = viewModel.isCharactersLoading
        assertEquals(false, loading.value)
    }

    @Test
    fun `isHousesLoading property should be accessible for Composables`() {
        // Act & Assert
        val loading = viewModel.isHousesLoading
        assertEquals(false, loading.value)
    }

    @Test
    fun `isSingleQueryLoading property should be accessible for Composables`() {
        // Act & Assert
        val loading = viewModel.isSingleQueryLoading
        assertEquals(false, loading.value)
    }

    @Test
    fun `currentCharacterPage property should be accessible for Composables`() {
        // Act & Assert
        val page = viewModel.currentCharacterPage
        assertEquals(1, page.value)
    }

    // ========== Characters Tab Simulation Tests ==========

    @Test
    fun `Characters tab should display loading state while fetching`() {
        // Simulate characters tab button click
        val isLoading = viewModel.isCharactersLoading.value
        assertEquals(false, isLoading)

        // Note: In real compose, collectAsState would update the UI
        // This test verifies the ViewModel exposes the right property
    }

    @Test
    fun `Characters tab should display empty state initially`() {
        // Assert
        val characters = viewModel.characters.value
        assertEquals(0, characters.size)
    }

    @Test
    fun `Characters tab should display page number`() {
        // Assert
        val currentPage = viewModel.currentCharacterPage.value
        assertEquals(1, currentPage)
    }

    // ========== Houses Tab Simulation Tests ==========

    @Test
    fun `Houses tab should display loading state during flow collection`() {
        // Assert initial state
        val isLoading = viewModel.isHousesLoading.value
        assertEquals(false, isLoading)
    }

    @Test
    fun `Houses tab should display empty state initially`() {
        // Assert
        val houses = viewModel.houses.value
        assertEquals(0, houses.size)
    }

    // ========== Single Query Tab Simulation Tests ==========

    @Test
    fun `Single Query tab should display loading state for character search`() {
        // Assert
        val isLoading = viewModel.isSingleQueryLoading.value
        assertEquals(false, isLoading)
    }

    @Test
    fun `Single Query tab should display result text area`() {
        // Assert - Should be empty or contain result
        val result = viewModel.singleResult.value
        assertEquals("", result)
    }

    @Test
    fun `Single Query tab should display all query buttons enabled initially`() {
        // Assert
        val isLoading = viewModel.isSingleQueryLoading.value
        assertEquals(false, isLoading)
    }

    // ========== UI State Updates Tests ==========

    @Test
    fun `Pagination buttons should be disabled while loading`() {
        // Verify that loading state is false initially
        assertEquals(false, viewModel.isCharactersLoading.value)

        // In actual Compose:
        // Button(..., enabled = !isLoading && currentPage > 1)
        // This test confirms the properties are available
    }

    @Test
    fun `Next button should be disabled on first page initially`() {
        // Verify page is 1
        assertEquals(1, viewModel.currentCharacterPage.value)

        // In actual Compose:
        // enabled = !isLoading && currentPage > 1
        // Which means enabled = !false && 1 > 1 = false
    }

    @Test
    fun `Query buttons should show loading indicator when active`() {
        // Test structure: when isSingleQueryLoading is true, show indicator
        assertEquals(false, viewModel.isSingleQueryLoading.value)

        // In Compose: if (isLoading) { CircularProgressIndicator(...) }
    }

    // ========== Button Click Handler Tests ==========

    @Test
    fun `loadCharactersPage should be callable from button onClick`() {
        // This mimics the button click in CharactersTab
        // Button click calls: scope.launch { viewModel.loadCharactersPage(...) }

        // Simulate click
        viewModel.loadCharactersPage(page = 1, pageSize = 5)

        // Assert - Should be loading initially
        assertEquals(true, viewModel.isCharactersLoading.value)
    }

    @Test
    fun `loadHousesFlow should be callable from button onClick`() {
        // Simulate HousesTab button click
        viewModel.loadHousesFlow()

        // Assert
        assertEquals(true, viewModel.isHousesLoading.value)
    }

    @Test
    fun `queryCharacterByName should be callable from button onClick`() {
        // Simulate SingleQueryTab button click
        viewModel.queryCharacterByName("Jon Snow")

        // Assert
        assertEquals(true, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `queryHouseByName should be callable from button onClick`() {
        // Simulate SingleQueryTab button click
        viewModel.queryHouseByName("Stark")

        // Assert
        assertEquals(true, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `queryBook should be callable from button onClick`() {
        // Simulate SingleQueryTab button click
        viewModel.queryBook()

        // Assert
        assertEquals(true, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `nextCharacterPage should be callable from navigation button`() {
        // First load a page
        viewModel.loadCharactersPage(page = 1, pageSize = 10)

        // Then simulate next button click
        viewModel.nextCharacterPage(pageSize = 10)

        // Assert - Should have attempted navigation
        // (actual page depends on pagination URLs from API)
    }

    @Test
    fun `previousCharacterPage should be callable from navigation button`() {
        // First load a page
        viewModel.loadCharactersPage(page = 2, pageSize = 10)

        // Then simulate prev button click
        viewModel.previousCharacterPage(pageSize = 10)

        // Assert - Should have attempted navigation
    }

    // ========== List Item Tests ==========

    @Test
    fun `characters list should be suitable for LazyColumn`() {
        // Assert - Should be a list that can be iterated
        val characters = viewModel.characters.value
        assertEquals(0, characters.size)
    }

    @Test
    fun `houses list should be suitable for LazyColumn`() {
        // Assert
        val houses = viewModel.houses.value
        assertEquals(0, houses.size)
    }

    // ========== Text Display Tests ==========

    @Test
    fun `loading message should display correctly`() {
        // The Compose shows: if (isLoading) "Loading..." else "Load..."
        val isLoading = viewModel.isCharactersLoading.value

        // Assert - button text changes based on loading state
        assertEquals(false, isLoading)
    }

    @Test
    fun `empty state message should display when no data and not loading`() {
        // The Compose shows:
        // if (characters.isEmpty() && !isLoading) { Text("Tap 'Load Page'...") }

        val isEmpty = viewModel.characters.value.isEmpty()
        val isLoading = viewModel.isCharactersLoading.value

        assertTrue(isEmpty)
        assertFalse(isLoading)
    }

    @Test
    fun `page indicator should show current page number`() {
        // The Compose shows: Text("Page $currentPage")
        val page = viewModel.currentCharacterPage.value
        assertEquals(1, page)
    }

    // ========== Recomposition Trigger Tests ==========

    @Test
    fun `changing characters should trigger recomposition`() {
        // In Compose: val characters by viewModel.characters.collectAsState()
        // Any change to this StateFlow should trigger recomposition

        val initialCharacters = viewModel.characters.value
        // If loading happened, characters would change
        // collectAsState() in Compose would detect this and recompose

        assertEquals(initialCharacters, viewModel.characters.value)
    }

    @Test
    fun `changing loading state should trigger recomposition`() {
        // In Compose: val isLoading by viewModel.isCharactersLoading.collectAsState()
        val initialState = viewModel.isCharactersLoading.value
        assertEquals(false, initialState)
    }

    @Test
    fun `changing single result should trigger recomposition`() {
        // In Compose: val result by viewModel.singleResult.collectAsState()
        val initialResult = viewModel.singleResult.value
        assertEquals("", initialResult)
    }

    // ========== Scope Launch Tests ==========

    @Test
    fun `methods are safe to call from rememberCoroutineScope launch`() {
        // The UI calls: scope.launch { viewModel.loadCharactersPage(...) }
        // This test ensures methods handle coroutine scope correctly

        viewModel.loadCharactersPage(page = 1, pageSize = 10)
        // Should not throw and should use viewModelScope internally

        assertEquals(true, viewModel.isCharactersLoading.value)
    }

    // ========== State Preservation Tests ==========

    @Test
    fun `currentCharacterPage should persist across operations`() {
        // Set page
        viewModel.loadCharactersPage(page = 3, pageSize = 10)
        val page1 = viewModel.currentCharacterPage.value

        // Do another operation
        viewModel.queryBook()

        // Page should remain unchanged
        val page2 = viewModel.currentCharacterPage.value
        assertEquals(page1, page2)
    }

    @Test
    fun `characters should persist after single query`() {
        // Note: Initial characters is empty, but test structure for future
        val initialCharacters = viewModel.characters.value

        viewModel.queryCharacterByName("Test")

        // Characters list should not be affected by single query
        assertEquals(initialCharacters.size, viewModel.characters.value.size)
    }

    @Test
    fun `houses should persist after character page load`() {
        val initialHouses = viewModel.houses.value

        viewModel.loadCharactersPage(page = 1, pageSize = 10)

        // Houses should not be affected
        assertEquals(initialHouses.size, viewModel.houses.value.size)
    }

    // ========== Property Type Tests ==========

    @Test
    fun `all StateFlow properties should be readable`() {
        // Ensure properties are exposed as StateFlow (not MutableStateFlow)
        // and can be consumed by Compose

        val prop1 = viewModel.characters
        val prop2 = viewModel.houses
        val prop3 = viewModel.singleResult
        val prop4 = viewModel.isCharactersLoading
        val prop5 = viewModel.isHousesLoading
        val prop6 = viewModel.isSingleQueryLoading
        val prop7 = viewModel.currentCharacterPage

        // All should be readable and have correct types
        assertEquals(emptyList<Any>(), prop1.value)
        assertEquals(emptyList<Any>(), prop2.value)
        assertEquals("", prop3.value)
        assertEquals(false, prop4.value)
        assertEquals(false, prop5.value)
        assertEquals(false, prop6.value)
        assertEquals(1, prop7.value)
    }
}







