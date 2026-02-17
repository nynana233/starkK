# StarkK SDK - Code Examples & Usage Patterns

This document provides practical code examples demonstrating various ways to use the StarkK SDK in your Android app.

## 1️⃣ Basic Setup

### Initialize the SDK Client
```kotlin
import com.starkk.sdk.StarkKClient

// Create with defaults
val client = StarkKClient.Builder()
    .build()

// Create with custom configuration
val client = StarkKClient.Builder()
    .enableLogging(true)
    .connectTimeout(60)  // seconds
    .readTimeout(60)
    .writeTimeout(60)
    .cacheSize(20L * 1024 * 1024)  // 20 MB
    .build()

// Create with custom base URL (useful for testing)
val client = StarkKClient.Builder()
    .baseUrl("https://custom.api.com/")
    .build()

// Create with custom OkHttpClient
val customHttpClient = OkHttpClient.Builder()
    .retryOnConnectionFailure(true)
    .build()

val client = StarkKClient.Builder()
    .okHttpClient(customHttpClient)
    .build()
```

## 2️⃣ Fetching Characters

### Fetch a Single Page of Characters
```kotlin
viewModelScope.launch {
    client.getCharacters(page = 1, pageSize = 25)
        .onSuccess { page ->
            val characters: List<Character> = page.items
            val currentPage: Int = page.currentPage
            val hasMore: Boolean = page.hasNext

            characters.forEach { character ->
                println("Name: ${character.name}")
                println("Gender: ${character.gender}")
                println("Culture: ${character.culture}")
            }
        }
        .onFailure { error ->
            println("Error: ${error.message}")
        }
}
```

### Fetch Characters Using Flow (Auto-Pagination)
```kotlin
import com.starkk.sdk.extensions.getCharactersAsFlow
import kotlinx.coroutines.flow.collect

viewModelScope.launch {
    try {
        client.getCharactersAsFlow(pageSize = 50).collect { characters ->
            // Called once per page, automatically walks all pages
            println("Received ${characters.size} characters")
            updateUI(characters)
        }
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

### Search Characters by Name
```kotlin
viewModelScope.launch {
    client.getCharactersByName("Jon Snow", pageSize = 10)
        .onSuccess { page ->
            if (page.items.isEmpty()) {
                showMessage("No characters found")
            } else {
                val character = page.items.first()
                displayCharacter(character)
            }
        }
}
```

### Advanced Character Search with Filters
```kotlin
viewModelScope.launch {
    client.getCharacters(
        name = "Daenerys",
        gender = "Female",
        culture = "Valyrian",
        isAlive = true,
        pageSize = 20
    )
        .onSuccess { page ->
            val characters = page.items
            // Process results
        }
}
```

### Cursor-Based Pagination
```kotlin
// Fetch page 1, then navigate forward using cursors
var currentPage: StarkKPage<Character>? = null

client.getCharacters(page = 1, pageSize = 20)
    .onSuccess { page ->
        currentPage = page
        displayCharacters(page.items)
    }

// Navigate to next page — no URLs needed
currentPage?.let { cursor ->
    client.nextCharacters(cursor)
        ?.onSuccess { nextPage ->
            currentPage = nextPage
            displayCharacters(nextPage.items)
        }
}

// Navigate to previous page
currentPage?.let { cursor ->
    client.previousCharacters(cursor)
        ?.onSuccess { prevPage ->
            currentPage = prevPage
            displayCharacters(prevPage.items)
        }
}
```

## 3️⃣ Fetching Houses

### Fetch All Houses (Flow-Based)
```kotlin
import com.starkk.sdk.extensions.getHousesAsFlow

viewModelScope.launch {
    client.getHousesAsFlow(pageSize = 50).collect { housePage ->
        housePage.forEach { house ->
            println("House: ${house.name}")
            println("Region: ${house.region}")
            println("Words: ${house.words}")
        }
    }
}
```

### Search Houses by Name
```kotlin
viewModelScope.launch {
    client.getHousesByName("House Stark")
        .onSuccess { page ->
            val house = page.items.firstOrNull()
            if (house != null) {
                println("Found: ${house.name}")
                println("Region: ${house.region}")
                println("Words: ${house.words}")
                println("Seats: ${house.seats}")
            }
        }
}
```

### Advanced House Filtering
```kotlin
viewModelScope.launch {
    client.getHouses(
        region = "The Reach",
        hasWords = true,
        hasTitles = true,
        pageSize = 20
    )
        .onSuccess { page ->
            page.items.forEach { house ->
                println("${house.name}: ${house.words}")
            }
        }
}
```

### House Cursor Navigation
```kotlin
client.getHouses(page = 1, pageSize = 10)
    .onSuccess { page ->
        // Navigate forward
        client.nextHouses(page)?.onSuccess { nextPage -> /* ... */ }
        // Navigate backward
        client.previousHouses(page)?.onSuccess { prevPage -> /* ... */ }
    }
```

## 4️⃣ Fetching Books

### Get Books (Single Page)
```kotlin
viewModelScope.launch {
    client.getBooks(page = 1, pageSize = 10)
        .onSuccess { page ->
            page.items.forEach { book ->
                println("Title: ${book.name}")
                println("ISBN: ${book.isbn}")
                println("Released: ${book.released}")
                println("Authors: ${book.authors}")
                println("Pages: ${book.numberOfPages}")
            }
        }
}
```

### Fetch Books Using Flow
```kotlin
import com.starkk.sdk.extensions.getBooksAsFlow

viewModelScope.launch {
    client.getBooksAsFlow(pageSize = 20).collect { bookPage ->
        bookPage.forEach { book ->
            saveBooksToDatabase(book)
        }
    }
}
```

### Search Books by Name
```kotlin
viewModelScope.launch {
    client.getBooksByName("A Game of Thrones")
        .onSuccess { page ->
            val book = page.items.firstOrNull()
            displayBookDetails(book)
        }
}
```

### Book Cursor Navigation
```kotlin
client.getBooks(page = 1, pageSize = 5)
    .onSuccess { page ->
        client.nextBooks(page)?.onSuccess { nextPage -> /* ... */ }
        client.previousBooks(page)?.onSuccess { prevPage -> /* ... */ }
    }
```

## 5️⃣ Error Handling Patterns

### Pattern 1: onSuccess / onFailure (Recommended)
```kotlin
client.getCharactersByName("Tyrion")
    .onSuccess { page ->
        // Success branch
        updateUI(page.items)
    }
    .onFailure { exception ->
        // Error branch — typed exceptions
        when (exception) {
            is StarkKException.HttpError -> {
                showError("HTTP ${exception.code}: ${exception.message}")
            }
            is StarkKException.NetworkError -> {
                showError("Network error: ${exception.message}")
            }
            is StarkKException.UnknownError -> {
                showError("Unexpected error: ${exception.message}")
            }
        }
    }
```

### Pattern 2: Direct Result Inspection
```kotlin
val result = client.getCharactersByName("Jon Snow")

when (result) {
    is StarkKPageResult.Success -> {
        val character = result.page.items.firstOrNull()
        displayCharacter(character)
    }
    is StarkKPageResult.Failure -> {
        showError(result.exception.message)
    }
}
```

### Pattern 3: try-catch with Flow
```kotlin
viewModelScope.launch {
    try {
        client.getCharactersAsFlow().collect { characters ->
            updateUI(characters)
        }
    } catch (e: StarkKException.HttpError) {
        showError("Server error: ${e.code}")
    } catch (e: StarkKException) {
        showError("Error: ${e.message}")
    }
}
```

## 6️⃣ Pagination Patterns

### Access Pagination State
```kotlin
client.getCharacters(pageSize = 10)
    .onSuccess { page ->
        println("Current page: ${page.currentPage}")
        println("Items on this page: ${page.items.size}")
        println("Has next page: ${page.hasNext}")
        println("Has previous page: ${page.hasPrevious}")
    }
```

### Fetch Specific Page
```kotlin
// Fetch page 5
client.getCharacters(page = 5, pageSize = 20)
```

### Navigate Pages with Cursors
```kotlin
// Store the current page cursor
var cursor: StarkKPage<Character>? = null

// Initial load
client.getCharacters(page = 1, pageSize = 20)
    .onSuccess { page -> cursor = page }

// Next page
cursor?.let { current ->
    client.nextCharacters(current)?.onSuccess { page -> cursor = page }
}

// Previous page
cursor?.let { current ->
    client.previousCharacters(current)?.onSuccess { page -> cursor = page }
}
```

## 7️⃣ ViewModel with StateFlow

### Complete ViewModel Example
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starkk.sdk.StarkKClient
import com.starkk.sdk.models.Character
import com.starkk.sdk.models.StarkKPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterViewModel : ViewModel() {
    private val client = StarkKClient.Builder().build()

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private var lastPage: StarkKPage<Character>? = null

    fun loadCharacters(page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            client.getCharacters(page = page, pageSize = pageSize)
                .onSuccess { starkKPage ->
                    _characters.value = starkKPage.items
                    _currentPage.value = starkKPage.currentPage
                    lastPage = starkKPage
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _isLoading.value = false
        }
    }

    fun nextPage() {
        val cursor = lastPage ?: return
        viewModelScope.launch {
            _isLoading.value = true
            client.nextCharacters(cursor)
                ?.onSuccess { starkKPage ->
                    _characters.value = starkKPage.items
                    _currentPage.value = starkKPage.currentPage
                    lastPage = starkKPage
                }
                ?.onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _characters.value = emptyList()

            try {
                client.getCharactersAsFlow().collect { page ->
                    _characters.value = _characters.value + page
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### Using in Compose
```kotlin
@Composable
fun CharacterListScreen(viewModel: CharacterViewModel) {
    val characters by viewModel.characters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCharacters()
    }

    when {
        isLoading -> CircularProgressIndicator()
        error != null -> Text("Error: $error")
        characters.isEmpty() -> Text("No characters")
        else -> {
            Column {
                Text("Page $currentPage")
                LazyColumn {
                    items(characters) { character ->
                        CharacterItem(character)
                    }
                }
                Row {
                    Button(onClick = { viewModel.nextPage() }) { Text("Next →") }
                }
            }
        }
    }
}
```

## 8️⃣ Combining Multiple Requests

### Sequential Requests
```kotlin
viewModelScope.launch {
    // First request
    client.getCharactersByName("Jon Snow")
        .onSuccess { characterPage ->
            val character = characterPage.items.firstOrNull()

            // Second request based on first result
            character?.let { char ->
                client.getHousesByName("House Stark")
                    .onSuccess { housePage ->
                        val house = housePage.items.firstOrNull()
                        displayCharacterAndHouse(char, house)
                    }
            }
        }
}
```

### Parallel Requests
```kotlin
viewModelScope.launch {
    val charactersDeferred = async {
        client.getCharacters(pageSize = 50)
    }
    val housesDeferred = async {
        client.getHouses(pageSize = 50)
    }

    val charactersResult = charactersDeferred.await()
    val housesResult = housesDeferred.await()

    charactersResult.onSuccess { chars ->
        housesResult.onSuccess { houses ->
            displayBoth(chars.items, houses.items)
        }
    }
}
```

## 9️⃣ Data Persistence with Room

### Model Class
```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val url: String,
    val name: String,
    val gender: String,
    val culture: String
)
```

### Save API Response to Database
```kotlin
viewModelScope.launch {
    client.getCharactersAsFlow().collect { page ->
        val entities = page.map { character ->
            CharacterEntity(
                url = character.url,
                name = character.name,
                gender = character.gender,
                culture = character.culture
            )
        }
        characterDao.insertAll(entities)
    }
}
```

## 🔟 Logging and Debugging

### Enable Request/Response Logging
```kotlin
val client = StarkKClient.Builder()
    .enableLogging(true)  // Logs all HTTP traffic
    .build()
```

### Custom Logging
```kotlin
viewModelScope.launch {
    val start = System.currentTimeMillis()

    client.getCharactersByName("Jon Snow")
        .onSuccess { page ->
            val duration = System.currentTimeMillis() - start
            Log.d("StarkK", "Fetched in ${duration}ms: ${page.items.size} items")
        }
        .onFailure { error ->
            val duration = System.currentTimeMillis() - start
            Log.e("StarkK", "Failed after ${duration}ms: ${error.message}")
        }
}
```

## 📋 Cheat Sheet

| Task | Method | Returns |
|------|--------|---------|
| Get page of characters | `getCharacters(page, pageSize)` | `StarkKPageResult<Character>` |
| Get all characters | `getCharactersAsFlow(pageSize)` | `Flow<List<Character>>` |
| Search character | `getCharactersByName(name)` | `StarkKPageResult<Character>` |
| Next character page | `nextCharacters(page)` | `StarkKPageResult<Character>?` |
| Previous character page | `previousCharacters(page)` | `StarkKPageResult<Character>?` |
| Get page of houses | `getHouses(page, pageSize)` | `StarkKPageResult<House>` |
| Get all houses | `getHousesAsFlow(pageSize)` | `Flow<List<House>>` |
| Search house | `getHousesByName(name)` | `StarkKPageResult<House>` |
| Next house page | `nextHouses(page)` | `StarkKPageResult<House>?` |
| Previous house page | `previousHouses(page)` | `StarkKPageResult<House>?` |
| Get page of books | `getBooks(page, pageSize)` | `StarkKPageResult<Book>` |
| Get all books | `getBooksAsFlow(pageSize)` | `Flow<List<Book>>` |
| Search book | `getBooksByName(name)` | `StarkKPageResult<Book>` |
| Next book page | `nextBooks(page)` | `StarkKPageResult<Book>?` |
| Previous book page | `previousBooks(page)` | `StarkKPageResult<Book>?` |
