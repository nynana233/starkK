# Contributing to StarkK SDK

Thanks for contributing to StarkK.
This document explains how to set up, develop, and submit changes for the SDK module.

## Development Setup

1. Open the repository in Android Studio.
2. Sync Gradle.
3. Build the SDK module:

```zsh
cd /Users/nate/AndroidStudioProjects/StarkK
./gradlew :starkk:assembleDebug
```

4. Run unit tests:

```zsh
cd /Users/nate/AndroidStudioProjects/StarkK
./gradlew :starkk:testDebugUnitTest
```

## Project Rules

- Keep public surface area minimal and intentional.
- Mark non-public SDK internals as `internal`.
- Prefer immutable data classes with safe default values.
- Use coroutines and `Flow` for async behavior.
- Do not introduce other third party libraries if existing ones will suffice.

## Code Style

- 100% Kotlin for SDK code.
- Follow Kotlin conventions and keep APIs null-safe.
- Add concise comments only when logic is not obvious.
- Keep naming clear and consumer-friendly for public APIs.

## Testing Guidance

When adding behavior:
- Add/adjust unit tests for parsing and client behavior.
- Cover pagination cases (`next`, `prev`, missing `Link` header).
- Cover failure scenarios (network exception, non-2xx response).

## Pull Request Checklist

Before opening a PR:
- [ ] Build passes for `:starkk`
- [ ] Unit tests pass for `:starkk`
- [ ] Public API changes are documented in `starkk/readme.md`
- [ ] New behavior includes tests
- [ ] No unrelated refactors or formatting-only churn

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification for all commits.

**Examples:**
- `feat(client): add house query filters to StarkKClient`
- `fix(parser): fix Link header parsing for last page`
- `docs: update API documentation`
- `test: add pagination edge case tests`
- `refactor(client): simplify response handling`

**Breaking Changes:**
Add `!` before the colon to indicate breaking changes:
- `feat!: redesign authentication API`
- `feat(api)!: change response format`

## Reporting Security Issues

Please do not open public issues for security vulnerabilities, if any.
Report privately to project maintainers.

