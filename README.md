# StarkK — Kotlin Android SDK for An API of Ice And Fire

A production-ready **Kotlin-first Android SDK** for [An API of Ice And Fire](https://anapioficeandfire.com/), featuring:

- 🔄 **Kotlin Coroutines & Flow** for async operations
- 📡 **Retrofit 2 + OkHttp 3** for networking
- 📦 **kotlinx.serialization** for JSON parsing
- 🛡️ **Type-safe error handling** with `StarkKPageResult<T>` and `StarkKException`
- 📄 **Cursor-based pagination** via `StarkKPage<T>`

---

## 🚀 Quick Start

### 1. Include the SDK

Add the starkk module to your project:

```gradle
dependencies {
    implementation 'com.github.nynana233:starkK:2.0'
}
```

### 2. Create a Client

```kotlin
val client = StarkKClient.Builder()
    .enableLogging(true)
    .build()
```

### 3. Fetch Data

#### Option A: Single Page with cursor-based pagination
```kotlin
viewModelScope.launch {
    client.getCharacters(page = 1, pageSize = 20)
        .onSuccess { page ->
            val characters = page.items
            val currentPage = page.currentPage
            val hasMore = page.hasNext
        }
        .onFailure { error -> /* handle StarkKException */ }
}
```

#### Option B: Navigate pages with cursors
```kotlin
// Fetch first page
val result = client.getCharacters(page = 1, pageSize = 20)
result.onSuccess { page ->
    // Navigate forward — no URLs needed
    val nextResult = client.nextCharacters(page)
    nextResult?.onSuccess { nextPage ->
        // nextPage.items, nextPage.currentPage, etc.
    }
}
```

#### Option C: All Pages (Flow-based, auto-pagination)
```kotlin
import com.starkk.sdk.extensions.getCharactersAsFlow

viewModelScope.launch {
    client.getCharactersAsFlow(pageSize = 50).collect { characters ->
        // Called once per page, automatically walks all pages
    }
}
```

---

## 🛡️ Error Handling

Errors are represented as `StarkKException` subtypes:

```kotlin
client.getCharacters()
    .onSuccess { page -> /* use page.items */ }
    .onFailure { error ->
        when (error) {
            is StarkKException.HttpError -> Log.e("SDK", "HTTP ${error.code}: ${error.message}")
            is StarkKException.NetworkError -> Log.e("SDK", "Network issue: ${error.message}")
            is StarkKException.UnknownError -> Log.e("SDK", "Unexpected: ${error.message}")
        }
    }
```

---

## 📋 Requirements

- **Minimum Android SDK:** 21
- **Target Android SDK:** 34 or higher

---

### Report Issues

Use the issue template at `starkk/issue-template.md`.

---

### Credits
* [Joakim Skoog](https://github.com/joakimskoog), [An API of Ice And Fire](https://anapioficeandfire.com/)

---

**Built with ❤️ using 100% Kotlin**
