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

