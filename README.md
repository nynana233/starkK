# StarkK — Kotlin Android SDK for An API of Ice And Fire

A production-ready **Kotlin-first Android SDK** for [An API of Ice And Fire](https://anapioficeandfire.com/), featuring:

- 🔄 **Kotlin Coroutines & Flow** for async operations
- 📡 **Retrofit 2 + OkHttp 3** for networking
- 📦 **kotlinx.serialization** for JSON parsing
- 🛡️ **Type-safe error handling** with `Result<T>`

---

## 🚀 Quick Start

### 1. Include the SDK

Add the starkk module to your project:

```gradle
dependencies {
    implementation(project(":starkk"))
}
```

### 2. Create a Client

```kotlin

val client = StarkKClient.Builder()
    .enableLogging(true)
    .build()

```

### 3. Fetch Data

#### Option A: Single Page (Result-based)
```kotlin
viewModelScope.launch {
    val result = client.getCharacters(page = 1, pageSize = 20)
    result
        .onSuccess { paginated -> /* use paginated.data */ }
        .onFailure { error -> /* handle error */ }
}
```

#### Option B: All Pages (Flow-based, auto-pagination)
```kotlin
import com.starkk.sdk.extensions.getCharactersAsFlow

viewModelScope.launch {
    client.getCharactersAsFlow(pageSize = 50).collect { characters ->
        // Called once per page, automatically walks all pages
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

