# StoryVerse

StoryVerse is a cross-platform mobile reading & AI storytelling application built with Kotlin Multiplatform.

## Architecture

StoryVerse adopts **Clean Architecture + MVVM** with native UI for each platform:

- **Shared KMP (`sharedLogic`) — Clean Architecture**:
  - **`domain`**: Entities, domain models, repository interfaces, and use cases / interactors.
  - **`data`**: Repository implementations, data sources (Ktor remote API, Room KMP database, memory cache), DTOs, and mappers.
  - **`core`**: Cross-cutting utilities, `AppResult` / `AppError`, `DispatcherProvider`, `AppLogger`.
  - **`di`**: Koin dependency injection modules.
- **Android App (`androidApp`) — MVVM**:
  - Native **Jetpack Compose** UI.
  - AndroidX ViewModels consuming shared domain use cases / repositories via `StateFlow`.
- **iOS App (`iosApp`) — MVVM**:
  - Native **SwiftUI** UI.
  - Swift ViewModels / Concurrency bridging `SharedLogic` static framework and `StateFlow`.

## Project Structure

- [/androidApp](./androidApp): Native Android application using Jetpack Compose.
- [/iosApp](./iosApp): Native iOS application using SwiftUI.
- [/sharedLogic](./sharedLogic): Shared Kotlin Multiplatform business, domain, and data logic.

## Building & Testing

- **Android Debug Build**: `./gradlew :androidApp:assembleDebug`
- **Shared KMP Tests**: `./gradlew :sharedLogic:allTests`
- **iOS Framework Build**: `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64`
- **iOS Xcode Build**: Open `iosApp/iosApp.xcodeproj` in Xcode or run `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build`