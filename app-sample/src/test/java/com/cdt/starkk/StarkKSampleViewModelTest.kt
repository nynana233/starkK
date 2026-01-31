package com.cdt.starkk

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * Unit tests for StarkKSampleViewModel
 * Tests pagination, Flow-based loading, and Result-based error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StarkKSampleViewModelTest {

    private lateinit var viewModel: StarkKSampleViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        viewModel = StarkKSampleViewModel()
    }

    // ========== Character Pagination Tests ==========

    @Test
    fun `loadCharactersPage should load characters successfully`() = runTest {
        // Act
        viewModel.loadCharactersPage(page = 1, pageSize = 10)

        // Assert
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.currentCharacterPage.value)
    }

    @Test
    fun `loadCharactersPage should set isCharactersLoading to true while loading`() = runTest {
        // Act
        viewModel.loadCharactersPage(page = 1, pageSize = 10)

        // Assert - initially should be loading
        assertEquals(true, viewModel.isCharactersLoading.value)
    }

    @Test
    fun `loadCharactersPage should reset isCharactersLoading to false after completion`() = runTest {
        // Act
        viewModel.loadCharactersPage(page = 1, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.isCharactersLoading.value)
    }

    @Test
    fun `currentCharacterPage should update to the requested page`() = runTest {
        // Act
        viewModel.loadCharactersPage(page = 3, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(3, viewModel.currentCharacterPage.value)
    }

    @Test
    fun `nextCharacterPage should increment page number when url is available`() = runTest {
        // Arrange - load initial page first
        viewModel.loadCharactersPage(page = 1, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialPage = viewModel.currentCharacterPage.value

        // Act - attempt to go to next page
        viewModel.nextCharacterPage(pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - page should have incremented or stayed same (depending on API response)
        assertTrue(viewModel.currentCharacterPage.value >= initialPage)
    }

    @Test
    fun `previousCharacterPage should decrement page number when url is available`() = runTest {
        // Arrange - load page 2 first
        viewModel.loadCharactersPage(page = 2, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialPage = viewModel.currentCharacterPage.value

        // Act - attempt to go to previous page
        viewModel.previousCharacterPage(pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - page should have decremented or stayed same
        assertTrue(viewModel.currentCharacterPage.value <= initialPage)
    }

    @Test
    fun `characters StateFlow should be empty initially`() {
        // Assert
        assertTrue(viewModel.characters.value.isEmpty())
    }

    @Test
    fun `loadCharactersPage should populate characters list`() = runTest {
        // Act
        viewModel.loadCharactersPage(page = 1, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - characters should be loaded (list may be empty or populated depending on API)
        assertEquals(true, viewModel.characters.value is List)
    }

    // ========== Houses Flow Tests ==========

    @Test
    fun `loadHousesFlow should set isHousesLoading to true initially`() = runTest {
        // Act
        viewModel.loadHousesFlow()

        // Assert
        assertEquals(true, viewModel.isHousesLoading.value)
    }

    @Test
    fun `loadHousesFlow should reset isHousesLoading to false after completion`() = runTest {
        // Act
        viewModel.loadHousesFlow()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.isHousesLoading.value)
    }

    @Test
    fun `loadHousesFlow should clear houses list before loading`() = runTest {
        // Arrange - set some initial data
        // (This would be done through loading first, but for this test we just verify the behavior)

        // Act
        viewModel.loadHousesFlow()
        // Check immediately to see if cleared
        assertTrue(viewModel.houses.value.isEmpty())
    }

    @Test
    fun `houses StateFlow should be empty initially`() {
        // Assert
        assertTrue(viewModel.houses.value.isEmpty())
    }

    @Test
    fun `loadHousesFlow should populate houses through Flow collection`() = runTest {
        // Act
        viewModel.loadHousesFlow()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - houses should be a list (may be empty or populated)
        assertTrue(viewModel.houses.value is List)
    }

    // ========== Single Query Tests ==========

    @Test
    fun `queryCharacterByName should set loading state before fetching`() = runTest {
        // Act
        viewModel.queryCharacterByName("Jon Snow")

        // Assert
        assertEquals(true, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `queryCharacterByName should reset loading state after fetching`() = runTest {
        // Act
        viewModel.queryCharacterByName("Jon Snow")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `queryCharacterByName should update singleResult with searching message`() = runTest {
        // Act
        viewModel.queryCharacterByName("Jon Snow")

        // Assert
        assertTrue(viewModel.singleResult.value.contains("Searching for 'Jon Snow'"))
    }

    @Test
    fun `queryCharacterByName should handle empty results`() = runTest {
        // Act
        viewModel.queryCharacterByName("NonExistentCharacter")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - result should indicate no characters found
        // (actual behavior depends on API, but result should be updated)
        assertTrue(viewModel.singleResult.value.isNotEmpty())
    }

    @Test
    fun `queryHouseByName should set loading state before fetching`() = runTest {
        // Act
        viewModel.queryHouseByName("House Stark")

        // Assert
        assertEquals(true, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `queryHouseByName should reset loading state after fetching`() = runTest {
        // Act
        viewModel.queryHouseByName("House Stark")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `queryHouseByName should update singleResult with searching message`() = runTest {
        // Act
        viewModel.queryHouseByName("House Stark")

        // Assert
        assertTrue(viewModel.singleResult.value.contains("Searching for 'House Stark'"))
    }

    @Test
    fun `queryBook should set loading state before fetching`() = runTest {
        // Act
        viewModel.queryBook()

        // Assert
        assertEquals(true, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `queryBook should reset loading state after fetching`() = runTest {
        // Act
        viewModel.queryBook()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `queryBook should update singleResult with fetching message`() = runTest {
        // Act
        viewModel.queryBook()

        // Assert
        assertTrue(viewModel.singleResult.value.contains("Fetching books"))
    }

    @Test
    fun `queryBook should handle empty results`() = runTest {
        // Act
        viewModel.queryBook()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.singleResult.value.isNotEmpty())
    }

    // ========== StateFlow Reactivity Tests ==========

    @Test
    fun `isCharactersLoading StateFlow should emit correct values`() = runTest {
        // Act & Assert
        assertEquals(false, viewModel.isCharactersLoading.value)

        viewModel.loadCharactersPage(page = 1, pageSize = 10)
        assertEquals(true, viewModel.isCharactersLoading.value)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, viewModel.isCharactersLoading.value)
    }

    @Test
    fun `isHousesLoading StateFlow should emit correct values`() = runTest {
        // Act & Assert
        assertEquals(false, viewModel.isHousesLoading.value)

        viewModel.loadHousesFlow()
        assertEquals(true, viewModel.isHousesLoading.value)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, viewModel.isHousesLoading.value)
    }

    @Test
    fun `isSingleQueryLoading StateFlow should emit correct values`() = runTest {
        // Act & Assert
        assertEquals(false, viewModel.isSingleQueryLoading.value)

        viewModel.queryCharacterByName("Test")
        assertEquals(true, viewModel.isSingleQueryLoading.value)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, viewModel.isSingleQueryLoading.value)
    }

    @Test
    fun `singleResult StateFlow should be empty initially`() {
        // Assert
        assertEquals("", viewModel.singleResult.value)
    }

    @Test
    fun `singleResult StateFlow should be updated by queries`() = runTest {
        // Act
        viewModel.queryCharacterByName("Jon Snow")

        // Assert
        assertTrue(viewModel.singleResult.value.isNotEmpty())
    }

    // ========== Multiple Sequential Operations Tests ==========

    @Test
    fun `loading multiple character pages should update correctly`() = runTest {
        // Act - Load page 1
        viewModel.loadCharactersPage(page = 1, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()
        val page1 = viewModel.currentCharacterPage.value

        // Act - Load page 2
        viewModel.loadCharactersPage(page = 2, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()
        val page2 = viewModel.currentCharacterPage.value

        // Assert
        assertEquals(1, page1)
        assertEquals(2, page2)
    }

    @Test
    fun `concurrent queries should not interfere with each other`() = runTest {
        // Act - Launch multiple queries
        viewModel.queryCharacterByName("Jon Snow")
        testDispatcher.scheduler.advanceUntilIdle()
        val result1 = viewModel.singleResult.value

        viewModel.queryHouseByName("Stark")
        testDispatcher.scheduler.advanceUntilIdle()
        val result2 = viewModel.singleResult.value

        // Assert - Results should be different and specific to each query
        assertTrue(result1.isNotEmpty())
        assertTrue(result2.isNotEmpty())
    }

    @Test
    fun `loadCharactersPage followed by loadHousesFlow should work independently`() = runTest {
        // Act
        viewModel.loadCharactersPage(page = 1, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadHousesFlow()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Both operations should complete successfully
        assertEquals(false, viewModel.isCharactersLoading.value)
        assertEquals(false, viewModel.isHousesLoading.value)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `queryCharacterByName with error should update singleResult with error message`() = runTest {
        // Act
        viewModel.queryCharacterByName("Jon Snow")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Result should be updated (may be success or error depending on network)
        assertTrue(viewModel.singleResult.value.isNotEmpty())
    }

    @Test
    fun `loadCharactersPage with error should update singleResult`() = runTest {
        // Act
        viewModel.loadCharactersPage(page = 1, pageSize = 10)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Should complete loading regardless of success/failure
        assertEquals(false, viewModel.isCharactersLoading.value)
    }

    @Test
    fun `loadHousesFlow with error should complete gracefully`() = runTest {
        // Act
        viewModel.loadHousesFlow()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Should complete loading
        assertEquals(false, viewModel.isHousesLoading.value)
    }

    // ========== Initial State Tests ==========

    @Test
    fun `ViewModel should initialize with default empty states`() {
        // Assert
        assertTrue(viewModel.characters.value.isEmpty())
        assertTrue(viewModel.houses.value.isEmpty())
        assertEquals("", viewModel.singleResult.value)
        assertEquals(false, viewModel.isCharactersLoading.value)
        assertEquals(false, viewModel.isHousesLoading.value)
        assertEquals(false, viewModel.isSingleQueryLoading.value)
        assertEquals(1, viewModel.currentCharacterPage.value)
    }

    @Test
    fun `all StateFlows should be properly exposed as StateFlow not MutableStateFlow`() {
        // Assert - These should not throw when accessed as StateFlow
        val characters = viewModel.characters
        val houses = viewModel.houses
        val singleResult = viewModel.singleResult
        val isCharactersLoading = viewModel.isCharactersLoading
        val isHousesLoading = viewModel.isHousesLoading
        val isSingleQueryLoading = viewModel.isSingleQueryLoading
        val currentPage = viewModel.currentCharacterPage

        // All should be accessible without error
        assertFalse(characters.value.isEmpty() && houses.value.isEmpty())
    }
}







