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
    val result = client.getCharacters(page = 1, pageSize = 25)
    
    result
        .onSuccess { paginated ->
            val characters: List<Character> = paginated.data
            val nextPageUrl: String? = paginated.next
            val prevPageUrl: String? = paginated.prev
            
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
    val result = client.getCharactersByName("Jon Snow", pageSize = 10)
    
    result.onSuccess { paginated ->
        if (paginated.data.isEmpty()) {
            showMessage("No characters found")
        } else {
            val character = paginated.data.first()
            displayCharacter(character)
        }
    }
}
```

### Advanced Character Search with Filters
```kotlin
viewModelScope.launch {
    val result = client.getCharacters(
        name = "Daenerys",
        gender = "Female",
        culture = "Valyrian",
        isAlive = true,
        pageSize = 20
    )
    
    result.onSuccess { paginated ->
        val characters = paginated.data
        // Process results
    }
}
```

### Manual Pagination
```kotlin
var allCharacters = mutableListOf<Character>()
var nextPageUrl: String? = "https://anapioficeandfire.com/api/characters?page=1"

while (nextPageUrl != null) {
    val result = client.getCharactersByUrl(nextPageUrl)
    
    result.onSuccess { paginated ->
        allCharacters.addAll(paginated.data)
        nextPageUrl = paginated.next
    }.onFailure { error ->
        nextPageUrl = null  // Stop on error
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
    val result = client.getHousesByName("House Stark")
    
    result.onSuccess { paginated ->
        val house = paginated.data.firstOrNull()
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
    val result = client.getHouses(
        region = "The Reach",
        hasWords = true,
        hasTitles = true,
        pageSize = 20
    )
    
    result.onSuccess { paginated ->
        paginated.data.forEach { house ->
            println("${house.name}: ${house.words}")
        }
    }
}
```

## 4️⃣ Fetching Books

### Get Books (Single Page)
```kotlin
viewModelScope.launch {
    val result = client.getBooks(page = 1, pageSize = 10)
    
    result.onSuccess { paginated ->
        paginated.data.forEach { book ->
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
    val result = client.getBooksByName("A Game of Thrones")
    
    result.onSuccess { paginated ->
        val book = paginated.data.firstOrNull()
        displayBookDetails(book)
    }
}
```

## 5️⃣ Error Handling Patterns

### Pattern 1: onSuccess / onFailure
```kotlin
val result = client.getCharactersByName("Tyrion")

result
    .onSuccess { paginated ->
        // Success branch
        updateUI(paginated.data)
    }
    .onFailure { exception ->
        // Error branch
        when (exception) {
            is StarkKClient.HttpException -> {
                showError("HTTP ${exception.code}: ${exception.message}")
            }
            else -> {
                showError("Network error: ${exception.message}")
            }
        }
    }
```

### Pattern 2: getOrNull() with Elvis Operator
```kotlin
val result = client.getCharactersByName("Jon Snow")
val character = result.getOrNull()?.data?.firstOrNull()

if (character != null) {
    displayCharacter(character)
} else {
    showError("Character not found or request failed")
}
```

### Pattern 3: try-catch with Flow
```kotlin
viewModelScope.launch {
    try {
        client.getCharactersAsFlow().collect { characters ->
            updateUI(characters)
        }
    } catch (e: StarkKClient.HttpException) {
        showError("Server error: ${e.code}")
    } catch (e: Exception) {
        showError("Error: ${e.message}")
    }
}
```

## 6️⃣ Pagination Patterns

### Access Pagination URLs
```kotlin
val result = client.getCharacters(pageSize = 10)

result.onSuccess { paginated ->
    paginated.apply {
        println("Current page has ${data.size} items")
        println("Has next page: ${hasNextPage}")
        println("Has prev page: ${hasPrevPage}")
        println("Next URL: $next")
        println("Prev URL: $prev")
        println("First URL: $first")
        println("Last URL: $last")
    }
}
```

### Fetch Specific Page
```kotlin
// Fetch page 5
val result = client.getCharacters(page = 5, pageSize = 20)
```

### Fetch Next/Previous Page
```kotlin
var currentResult = client.getCharacters(page = 1, pageSize = 20).getOrNull()

// Fetch next page
currentResult?.next?.let { nextUrl ->
    currentResult = client.getCharactersByUrl(nextUrl).getOrNull()
}

// Fetch previous page
currentResult?.prev?.let { prevUrl ->
    currentResult = client.getCharactersByUrl(prevUrl).getOrNull()
}
```

## 7️⃣ ViewModel with StateFlow

### Complete ViewModel Example
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

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

    LaunchedEffect(Unit) {
        viewModel.loadCharacters()
    }

    when {
        isLoading -> CircularProgressIndicator()
        error != null -> Text("Error: $error")
        characters.isEmpty() -> Text("No characters")
        else -> {
            LazyColumn {
                items(characters) { character ->
                    CharacterItem(character)
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
    val charactersResult = client.getCharactersByName("Jon Snow")
    
    charactersResult.onSuccess { characterPage ->
        val character = characterPage.data.firstOrNull()
        
        // Second request based on first result
        character?.let { char ->
            val houseResult = client.getHousesByName("House Stark")
            houseResult.onSuccess { housePage ->
                val house = housePage.data.firstOrNull()
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
            displayBoth(chars, houses)
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
    
    val result = client.getCharactersByName("Jon Snow")
    
    val duration = System.currentTimeMillis() - start
    
    result
        .onSuccess { paginated ->
            Log.d("StarkK", "Fetched in ${duration}ms: ${paginated.data.size} items")
        }
        .onFailure { error ->
            Log.e("StarkK", "Failed after ${duration}ms: ${error.message}")
        }
}
```

## 📋 Cheat Sheet

| Task | Method | Returns |
|------|--------|---------|
| Get page of characters | `getCharacters(page, pageSize)` | `Result<PaginatedResult<Character>>` |
| Get all characters | `getCharactersAsFlow(pageSize)` | `Flow<List<Character>>` |
| Search character | `getCharactersByName(name)` | `Result<PaginatedResult<Character>>` |
| Get page of houses | `getHouses(page, pageSize)` | `Result<PaginatedResult<House>>` |
| Get all houses | `getHousesAsFlow(pageSize)` | `Flow<List<House>>` |
| Search house | `getHousesByName(name)` | `Result<PaginatedResult<House>>` |
| Get page of books | `getBooks(page, pageSize)` | `Result<PaginatedResult<Book>>` |
| Get all books | `getBooksAsFlow(pageSize)` | `Flow<List<Book>>` |
| Search book | `getBooksByName(name)` | `Result<PaginatedResult<Book>>` |


